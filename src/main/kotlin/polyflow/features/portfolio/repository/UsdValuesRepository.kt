package polyflow.features.portfolio.repository

import polyflow.features.portfolio.model.id.Erc20TokenId
import polyflow.features.portfolio.model.id.NftTokenId
import polyflow.features.portfolio.model.result.FungibleTokenUsdValue
import polyflow.features.portfolio.model.result.NativeAssetUsdValue
import polyflow.features.portfolio.model.result.NftTokenUsdValue
import polyflow.util.ChainId

interface UsdValuesRepository {
    fun upsertNativeAssetUsdValue(value: NativeAssetUsdValue)
    fun upsertFungibleTokenUsdValue(value: FungibleTokenUsdValue)
    fun upsertNftTokenUsdValue(value: NftTokenUsdValue)
    fun fetchNativeAssetValues(chainIds: List<ChainId>): Map<ChainId, NativeAssetUsdValue>
    fun fetchFungibleTokenValues(ids: List<Erc20TokenId>): Map<Erc20TokenId, FungibleTokenUsdValue>
    fun fetchNftTokenValues(ids: List<NftTokenId>): Map<NftTokenId, NftTokenUsdValue>
}
