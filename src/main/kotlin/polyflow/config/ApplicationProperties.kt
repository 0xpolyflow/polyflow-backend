@file:Suppress("MagicNumber")

package polyflow.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import polyflow.util.ChainId
import java.math.BigInteger
import java.security.interfaces.RSAPrivateCrtKey
import java.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "polyflow")
class ApplicationProperties {
    var chain: Map<ChainId, ChainProperties> = emptyMap()
    var infuraId: String = ""
}

@ConstructorBinding
data class ChainProperties(
    val name: String,
    val rpcUrl: String,
    val infuraUrl: String?,
    val startBlockNumber: BigInteger?,
    val minBlockConfirmationsForCaching: BigInteger?,
    val chainExplorerApiUrl: String?,
    val chainExplorerApiKey: String?,
    val latestBlockCacheDuration: Duration = 5.seconds.toJavaDuration()
)

@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.jwt")
data class JwtProperties(
    val privateKey: RSAPrivateCrtKey,
    val tokenValidity: Duration
)

@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.user")
data class UserAccountProperties(
    val verificationTokenDuration: Duration = 24.hours.toJavaDuration(),
    val passwordResetTokenDuration: Duration = 24.hours.toJavaDuration()
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
    val redirectDomain: String
)

@ConstructorBinding
@ConfigurationProperties("polyflow.mail")
data class PolyflowMailProperties(
    val from: String
)
