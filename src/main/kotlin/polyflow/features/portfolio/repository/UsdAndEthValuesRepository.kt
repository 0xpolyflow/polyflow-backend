package polyflow.features.portfolio.repository

import polyflow.features.portfolio.model.id.Erc20TokenId
import polyflow.features.portfolio.model.id.NftTokenId
import polyflow.features.portfolio.model.result.FungibleTokenUsdValue
import polyflow.features.portfolio.model.result.NativeAssetUsdValue
import polyflow.features.portfolio.model.result.NftTokenEthValue
import polyflow.util.ChainId

interface UsdAndEthValuesRepository {
    fun upsertNativeAssetUsdValue(value: NativeAssetUsdValue)
    fun upsertFungibleTokenUsdValue(value: FungibleTokenUsdValue)
    fun upsertNftTokenEthValue(value: NftTokenEthValue)
    fun fetchNativeAssetValues(chainIds: List<ChainId>): Map<ChainId, NativeAssetUsdValue>
    fun fetchFungibleTokenValues(ids: List<Erc20TokenId>): Map<Erc20TokenId, FungibleTokenUsdValue>
    fun fetchNftTokenValues(ids: List<NftTokenId>): Map<NftTokenId, NftTokenEthValue>
}
