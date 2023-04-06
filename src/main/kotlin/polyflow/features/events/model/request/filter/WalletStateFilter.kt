package polyflow.features.events.model.request.filter

import polyflow.config.validation.ValidEthAddress
import polyflow.config.validation.ValidUint256
import java.math.BigInteger
import javax.validation.constraints.PositiveOrZero

data class WalletStateFilter(
    @field:ValidEthAddress
    val walletAddress: String?,

    @field:ValidUint256
    val gasBalance: BigInteger?,

    @field:ValidUint256
    val nonce: BigInteger?,

    @field:PositiveOrZero
    val networkId: Long?
)
