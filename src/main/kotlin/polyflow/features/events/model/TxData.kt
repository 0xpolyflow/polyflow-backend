package polyflow.features.events.model

import polyflow.config.validation.MaxFunctionDataSize
import polyflow.config.validation.ValidEthAddress
import polyflow.config.validation.ValidEthTxHash
import polyflow.config.validation.ValidHexString
import polyflow.config.validation.ValidUint256
import polyflow.generated.jooq.enums.TxStatus
import java.math.BigInteger
import javax.validation.constraints.NotNull

data class TxData(
    @field:NotNull
    @field:ValidEthAddress
    val from: String,

    @field:ValidEthAddress
    val to: String?,

    @field:ValidUint256
    val value: BigInteger?,

    @field:ValidHexString
    @field:MaxFunctionDataSize
    val input: String?,

    @field:ValidUint256
    val nonce: BigInteger?,

    @field:ValidUint256
    val gas: BigInteger?,

    @field:ValidUint256
    val gasPrice: BigInteger?,

    @field:ValidUint256
    val maxFeePerGas: BigInteger?,

    @field:ValidUint256
    val maxPriorityFeePerGas: BigInteger?,

    @field:ValidHexString
    val v: String?,

    @field:ValidHexString
    val r: String?,

    @field:ValidHexString
    val s: String?,

    @field:ValidEthTxHash
    val hash: String?,

    @field:NotNull
    val status: TxStatus
)
