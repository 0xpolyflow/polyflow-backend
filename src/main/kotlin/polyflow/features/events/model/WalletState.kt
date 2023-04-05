package polyflow.features.events.model

import polyflow.config.validation.ValidEthAddress
import polyflow.config.validation.ValidUint256
import java.math.BigInteger
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

data class WalletState(
    @field:NotNull
    @field:ValidEthAddress
    val walletAddress: String,

    @field:NotNull
    @field:ValidUint256
    val gasBalance: BigInteger,

    @field:NotNull
    @field:ValidUint256
    val nonce: BigInteger,

    @field:NotNull
    @field:PositiveOrZero
    val networkId: Long
)
