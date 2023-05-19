package polyflow.features.portfolio.model.result

import polyflow.util.Balance
import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.NftId
import polyflow.util.UsdValue
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress

data class WalletPortfolioDataAndValue(
    val walletAddress: WalletAddress,
    val nativeAssetBalances: List<AssetBalanceAndValue>,
    val fungibleTokenBalances: List<FungibleTokenBalanceAndValue>,
    val nftTokenBalances: List<NftTokenBalanceAndValue>,
    val failedRpcCalls: List<AssetRpcCall>,
    val totalValue: UsdValue,
    val updatedAt: UtcDateTime
)

data class AssetBalanceAndValue(
    val name: String,
    val chainId: ChainId,
    val amount: Balance,
    val value: UsdValue
)

data class FungibleTokenBalanceAndValue(
    val name: String,
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val amount: Balance,
    val value: UsdValue
)

data class NftIdAndValue(
    val id: NftId,
    val value: UsdValue
)

data class NftTokenBalanceAndValue(
    val name: String,
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val ownsAsset: Boolean,
    val ownedAssets: List<NftIdAndValue>,
    val totalValue: UsdValue
)
