package polyflow.features.portfolio.model.result

import polyflow.util.ChainId
import polyflow.util.Decimals
import polyflow.util.UsdValue
import polyflow.util.UtcDateTime

data class NativeAssetUsdValue(
    val chainId: ChainId,
    val usdValue: UsdValue,
    val decimals: Decimals,
    val updatedAt: UtcDateTime
)
