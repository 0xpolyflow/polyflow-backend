package polyflow.features.events.model.request.filter

import polyflow.config.validation.ValidUint256
import java.math.BigInteger
import javax.validation.constraints.PositiveOrZero

data class NetworkStateFilter(
    @field:PositiveOrZero
    val chainId: Long?,

    @field:ValidUint256
    val gasPrice: BigInteger?,

    @field:ValidUint256
    val blockHeight: BigInteger?
)
