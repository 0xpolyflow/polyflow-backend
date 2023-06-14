@file:Suppress("MagicNumber")

package polyflow.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import polyflow.util.ChainId
import java.nio.file.Path
import java.security.interfaces.RSAPrivateCrtKey
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import java.time.Duration as JavaDuration

@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "polyflow")
class ApplicationProperties {
    var chain: Map<ChainId, ChainProperties> = emptyMap()
    var debugAccountId: UUID? = null
}

@ConstructorBinding
data class ChainProperties(
    val rpcKey: String
)

@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.jwt")
data class JwtProperties(
    val privateKey: RSAPrivateCrtKey,
    val tokenValidity: JavaDuration
)

@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.user")
data class UserAccountProperties(
    val verificationTokenDuration: JavaDuration = 24.hours.toJavaDuration(),
    val passwordResetTokenDuration: JavaDuration = 24.hours.toJavaDuration()
)

@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.email")
data class EmailProperties(
    val verificationEmailSubject: String,
    val verificationEmailTemplate: String,
    val passwordResetEmailSubject: String,
    val passwordResetEmailTemplate: String
)

@ConstructorBinding
@ConfigurationProperties("polyflow.stripe")
data class StripeProperties(
    val apiKey: String,
    val endpointSecret: String,
    val redirectDomain: String,
    val promoCode: String?
)

@ConstructorBinding
@ConfigurationProperties("polyflow.mail")
data class PolyflowMailProperties(
    val from: String
)

@ConstructorBinding
@ConfigurationProperties("polyflow.portfolio")
data class PortfolioProperties(
    val chainDefinitionsFile: Path?,
    val tokenDefinitionsFile: Path?,
    val balanceRefreshInterval: JavaDuration = 7.days.toJavaDuration(),
    val priceRefreshInterval: JavaDuration = 1.days.toJavaDuration()
)
