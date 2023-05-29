package polyflow.features.portfolio.model.result

import polyflow.util.Balance
import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.NftId
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress

data class WalletPortfolioData(
    val walletAddress: WalletAddress,
    val nativeAssetBalances: List<AssetBalance>,
    val fungibleTokenBalances: List<FungibleTokenBalance>,
    val nftTokenBalances: List<NftTokenBalance>,
    val failedRpcCalls: List<AssetRpcCall>,
    val updatedAt: UtcDateTime
)

data class AssetBalance(
    val chainId: ChainId,
    val amount: Balance
)

data class FungibleTokenBalance(
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val amount: Balance
)

data class NftTokenBalance(
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val ownsAsset: Boolean,
    val ownedAssets: List<NftId>,
    val amountOfOwnedAssets: Balance
)

data class AssetRpcCall(
    val tokenAddress: ContractAddress,
    val chainId: ChainId,
    val isNft: Boolean
)
