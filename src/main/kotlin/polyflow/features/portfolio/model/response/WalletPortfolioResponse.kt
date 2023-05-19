package polyflow.features.portfolio.model.response

import polyflow.features.portfolio.model.result.WalletPortfolioDataAndValue
import java.math.BigDecimal
import java.math.BigInteger
import java.time.OffsetDateTime

data class WalletPortfolioResponse(
    val walletAddress: String,
    val nativeAssetBalances: List<AssetBalanceAndValueResponse>,
    val fungibleTokenBalances: List<FungibleTokenBalanceAndValueResponse>,
    val nftTokenBalances: List<NftTokenBalanceAndValueResponse>,
    val failedRpcCalls: List<AssetRpcCallResponse>,
    val totalValue: BigDecimal,
    val updatedAt: OffsetDateTime
) {
    constructor(data: WalletPortfolioDataAndValue) : this(
        walletAddress = data.walletAddress.rawValue,
        nativeAssetBalances = data.nativeAssetBalances.map {
            AssetBalanceAndValueResponse(
                name = it.name,
                chainId = it.chainId.value,
                amount = it.amount.rawValue,
                value = it.value.value
            )
        },
        fungibleTokenBalances = data.fungibleTokenBalances.map {
            FungibleTokenBalanceAndValueResponse(
                name = it.name,
                tokenAddress = it.tokenAddress.rawValue,
                chainId = it.chainId.value,
                amount = it.amount.rawValue,
                value = it.value.value
            )
        },
        nftTokenBalances = data.nftTokenBalances.map {
            NftTokenBalanceAndValueResponse(
                name = it.name,
                tokenAddress = it.tokenAddress.rawValue,
                chainId = it.chainId.value,
                ownsAsset = it.ownsAsset,
                ownedAssets = it.ownedAssets.map { a ->
                    NftIdAndValueResponse(
                        id = a.id.value,
                        value = a.value.value
                    )
                },
                totalValue = it.totalValue.value
            )
        },
        failedRpcCalls = data.failedRpcCalls.map {
            AssetRpcCallResponse(
                tokenAddress = it.tokenAddress.rawValue,
                chainId = it.chainId.value,
                isNft = it.isNft
            )
        },
        totalValue = data.totalValue.value,
        updatedAt = data.updatedAt.value
    )
}

data class AssetBalanceAndValueResponse(
    val name: String,
    val chainId: Long,
    val amount: BigInteger,
    val value: BigDecimal
)

data class FungibleTokenBalanceAndValueResponse(
    val name: String,
    val tokenAddress: String,
    val chainId: Long,
    val amount: BigInteger,
    val value: BigDecimal
)

data class NftIdAndValueResponse(
    val id: BigInteger,
    val value: BigDecimal
)

data class NftTokenBalanceAndValueResponse(
    val name: String,
    val tokenAddress: String,
    val chainId: Long,
    val ownsAsset: Boolean,
    val ownedAssets: List<NftIdAndValueResponse>,
    val totalValue: BigDecimal
)

data class AssetRpcCallResponse(
    val tokenAddress: String,
    val chainId: Long,
    val isNft: Boolean
)
