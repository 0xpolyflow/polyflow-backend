package polyflow.features.portfolio.model.id

import polyflow.util.ChainId
import polyflow.util.ContractAddress

data class Erc20TokenId(
    val chainId: ChainId,
    val tokenAddress: ContractAddress
)
