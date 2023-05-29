package polyflow.features.portfolio.model.result

import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.EthValue
import polyflow.util.UtcDateTime

data class NftTokenEthValue(
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val ethValue: EthValue,
    val updatedAt: UtcDateTime
)
