package polyflow.features.billing.controller

import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Price
import com.stripe.model.Product
import com.stripe.model.StripeObject
import com.stripe.model.Subscription
import com.stripe.net.Webhook
import com.stripe.param.PriceListParams
import com.stripe.param.checkout.SessionCreateParams.LineItem
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import polyflow.config.StripeProperties
import polyflow.config.binding.annotation.UserBinding
import polyflow.exception.StripeCustomerIdMissing
import polyflow.exception.WebhookException
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.UserId
import java.net.URI
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
        @RequestParam("lookup_key") lookupKey: String
    ): ResponseEntity<Void> {
        val priceParams = PriceListParams.builder().apply {
            addLookupKeys(lookupKey)
        }.build()

        val prices = Price.list(priceParams)
        val params = CheckoutSessionCreateParams.builder().apply {
            user.stripeCustomerId?.let { setCustomer(it) }

            addLineItem(LineItem.builder().setPrice(prices.data[0].id).setQuantity(1L).build())
            setMode(CheckoutSessionCreateParams.Mode.SUBSCRIPTION)
            setSuccessUrl(stripeProperties.redirectDomain + "/payments/success")
            setCancelUrl(stripeProperties.redirectDomain + "/payments/cancel")
        }.build()

        val session = CheckoutSession.create(params)

        userRepository.setStripeCustomerId(user.id, session.customer)

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create(session.url))
            .build()
    }

    @PostMapping("/v1/stripe/create-portal-session")
    fun createPortalSession(
        @UserBinding user: User
    ): ResponseEntity<Void> {
        if (user.stripeCustomerId == null) {
            throw StripeCustomerIdMissing()
        }

        val params = PortalSessionCreateParams.Builder().apply {
            setReturnUrl(stripeProperties.redirectDomain)
            setCustomer(user.stripeCustomerId)
        }.build()

        val session = BillingSession.create(params)

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create(session.url))
            .build()
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

    private fun StripeObject.asSubscription(): Subscription = this as? Subscription ?: throw WebhookException()

    private fun Subscription.userId(): UserId? = userRepository.getByStripeCustomerId(this.customer)?.id

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
