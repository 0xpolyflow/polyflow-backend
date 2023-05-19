package polyflow.features.portfolio.model.result

import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.NftId
import polyflow.util.UsdValue
import polyflow.util.UtcDateTime

data class NftTokenUsdValue(
    val tokenAddress: ContractAddress,
    val tokenId: NftId,
    val chainId: ChainId,
    val usdValue: UsdValue,
    val updatedAt: UtcDateTime
)
