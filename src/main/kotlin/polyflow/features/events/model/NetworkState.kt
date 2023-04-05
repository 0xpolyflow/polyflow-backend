package polyflow.features.events.model

import polyflow.config.validation.ValidUint256
import java.math.BigInteger
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

data class NetworkState(
    @field:NotNull
    @field:PositiveOrZero
    val chainId: Long,

    @field:NotNull
    @field:ValidUint256
    val gasPrice: BigInteger,

    @field:NotNull
    @field:ValidUint256
    val blockHeight: BigInteger
)
