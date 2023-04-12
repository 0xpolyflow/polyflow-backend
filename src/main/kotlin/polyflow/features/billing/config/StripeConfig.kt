package polyflow.features.billing.config

import com.stripe.Stripe
import mu.KLogging
import org.springframework.context.annotation.Configuration
import polyflow.config.StripeProperties
import javax.annotation.PostConstruct

@Configuration
class StripeConfig(private val stripeProperties: StripeProperties) {

    companion object : KLogging()

    @PostConstruct
    fun configureStripe() {
        logger.info { "Configuring Stripe payment API..." }
        Stripe.apiKey = stripeProperties.apiKey
    }
}
