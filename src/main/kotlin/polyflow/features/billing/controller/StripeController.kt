package polyflow.features.billing.controller

import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import com.stripe.model.Price
import com.stripe.model.Product
import com.stripe.model.PromotionCode
import com.stripe.model.StripeObject
import com.stripe.model.Subscription
import com.stripe.net.Webhook
import com.stripe.param.PromotionCodeListParams
import com.stripe.param.checkout.SessionCreateParams.LineItem
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData
import mu.KLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import polyflow.config.StripeProperties
import polyflow.config.binding.annotation.UserBinding
import polyflow.exception.PriceObjectNotFoundException
import polyflow.exception.StripeSessionIdMissingException
import polyflow.exception.WebhookException
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.UserId
import com.stripe.model.billingportal.Session as BillingSession
import com.stripe.model.checkout.Session as CheckoutSession
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams

@RestController
class StripeController(
    private val userRepository: UserRepository,
    private val stripeProperties: StripeProperties
) {

    companion object : KLogging() {
        private const val DOMAIN_LIMIT_KEY = "domain_limit"
        private const val SEAT_LIMIT_KEY = "seat_limit"
        private val ZERO_LIMITS = Limits(0, 0)

        private data class Limits(val domainLimit: Int, val seatLimit: Int)
    }

    @PostMapping("/v1/stripe/create-checkout-session")
    fun createCheckoutSession(
        @UserBinding user: User,
        @RequestParam("price_id", required = true) priceId: String,
        @RequestParam("promo_code", required = false) promoCode: String?
    ): ResponseEntity<String> {
        val price = try {
            Price.retrieve(priceId)
        } catch (se: StripeException) {
            throw PriceObjectNotFoundException(priceId)
        }

        val coupon = (promoCode ?: stripeProperties.promoCode)?.retrieveCoupon()

        val params = CheckoutSessionCreateParams.builder().apply {
            addLineItem(LineItem.builder().setPrice(price.id).setQuantity(1L).build())
            setCustomerEmail(user.email)
            setMode(CheckoutSessionCreateParams.Mode.SUBSCRIPTION)
            setSuccessUrl(stripeProperties.redirectDomain + "/payments/success")
            setCancelUrl(stripeProperties.redirectDomain + "/payments/cancel")
            setSubscriptionData(
                SubscriptionData.builder()
                    .setTrialPeriodDays(30L)
                    .build()
            )
            setPaymentMethodCollection(CheckoutSessionCreateParams.PaymentMethodCollection.ALWAYS)

            if (coupon != null) {
                addDiscount(
                    CheckoutSessionCreateParams.Discount.builder()
                        .setCoupon(coupon)
                        .build()
                )
            } else {
                setAllowPromotionCodes(true)
            }
        }.build()

        val session = CheckoutSession.create(params)

        userRepository.setStripeSessionId(user.id, session.id)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(session.url)
    }

    @PostMapping("/v1/stripe/create-portal-session")
    fun createPortalSession(
        @UserBinding user: User
    ): ResponseEntity<String> {
        val customerId = user.resolveStripeCustomerId()

        val params = PortalSessionCreateParams.Builder().apply {
            setReturnUrl(stripeProperties.redirectDomain)
            setCustomer(customerId)
        }.build()

        val billingSession = BillingSession.create(params)

        userRepository.clearStripeSessionId(user.id)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(billingSession.url)
    }

    @PostMapping("/v1/stripe/webhook")
    fun webhook(
        @RequestHeader("Stripe-Signature") stripeSignature: String,
        @RequestBody payload: String
    ) {
        val event = try {
            Webhook.constructEvent(payload, stripeSignature, stripeProperties.endpointSecret)
        } catch (e: SignatureVerificationException) {
            logger.error { "Unable to process webhook event, payload: $payload, stripeSignature: $stripeSignature" }
            throw WebhookException()
        }

        val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)

        logger.debug { "Got webhook event: ${event.type}" }

        when (event.type) {
            "customer.subscription.deleted" -> {
                stripeObject.asSubscription().userId()
                    ?.updateAccountLimits(ZERO_LIMITS)
            }

            "customer.subscription.trial_will_end" -> {
                // TODO send email if we want to
            }

            "customer.subscription.created" -> {
                val subscription = stripeObject.asSubscription()

                subscription.userId()
                    ?.updateAccountLimits(subscription.limits())
            }

            "customer.subscription.updated" -> {
                val subscription = stripeObject.asSubscription()

                subscription.userId()
                    ?.updateAccountLimits(subscription.limits())
            }

            else -> {
                logger.debug { "Unhandled webhook event: ${event.type}" }
            }
        }
    }

    private fun String.retrieveCoupon(): String? =
        if (this.startsWith("promo_")) {
            PromotionCode.retrieve(this)?.coupon?.id
        } else {
            PromotionCode.list(
                PromotionCodeListParams.builder().setCode(this).build()
            )?.data?.getOrNull(0)?.coupon?.id
        }

    private fun User.resolveStripeCustomerId(): String =
        this.stripeCustomerId ?: run {
            if (this.stripeSessionId == null) {
                throw StripeSessionIdMissingException()
            }

            val checkoutSession = CheckoutSession.retrieve(this.stripeSessionId)
            val customerId = checkoutSession.customer

            userRepository.setStripeCustomerId(this.id, customerId)

            customerId
        }

    private fun StripeObject.asSubscription(): Subscription = this as? Subscription ?: throw WebhookException()

    private fun Subscription.userId(): UserId? =
        userRepository.getByStripeCustomerId(this.customer)?.id
            ?: Customer.retrieve(this.customer)?.email?.let(userRepository::getByEmail)?.id
            ?: run {
                logger.warn { "No user found for Stripe customer id (or email): ${this.customer}" }
                null
            }

    private fun Subscription.limits(): Limits =
        this.items.data.getOrNull(0)?.let { data ->
            val product = Product.retrieve(data.plan.product)
            val domainLimit = product.metadataIntValue(DOMAIN_LIMIT_KEY) ?: throw WebhookException()
            val seatLimit = product.metadataIntValue(SEAT_LIMIT_KEY) ?: throw WebhookException()

            Limits(
                domainLimit = domainLimit,
                seatLimit = seatLimit
            )
        } ?: ZERO_LIMITS

    private fun Product.metadataIntValue(key: String): Int? {
        val result = this.metadata[key]?.toIntOrNull()

        if (result == null) {
            logger.warn { "Metadata key $key is not set correctly for product with id: $id, name: $name, skipping" }
        }

        return result
    }

    private fun UserId.updateAccountLimits(limits: Limits) {
        userRepository.updateAccountLimits(
            userId = this,
            domainLimit = limits.domainLimit,
            seatLimit = limits.seatLimit
        )
    }
}
