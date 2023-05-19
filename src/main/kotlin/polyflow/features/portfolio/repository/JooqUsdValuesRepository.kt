package polyflow.features.portfolio.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import polyflow.features.portfolio.model.id.Erc20TokenId
import polyflow.features.portfolio.model.id.NftTokenId
import polyflow.features.portfolio.model.result.FungibleTokenUsdValue
import polyflow.features.portfolio.model.result.NativeAssetUsdValue
import polyflow.features.portfolio.model.result.NftTokenUsdValue
import polyflow.generated.jooq.tables.FungibleTokenUsdValueTable
import polyflow.generated.jooq.tables.NativeAssetUsdValueTable
import polyflow.generated.jooq.tables.NftTokenUsdValueTable
import polyflow.generated.jooq.tables.records.FungibleTokenUsdValueRecord
import polyflow.generated.jooq.tables.records.NativeAssetUsdValueRecord
import polyflow.generated.jooq.tables.records.NftTokenUsdValueRecord
import polyflow.util.ChainId

@Repository
class JooqUsdValuesRepository(private val dslContext: DSLContext) : UsdValuesRepository { // TODO test

    companion object : KLogging()

    override fun upsertNativeAssetUsdValue(value: NativeAssetUsdValue) {
        logger.info { "Upsert native asset USD value: $value" }

        val record = NativeAssetUsdValueRecord(
            chainId = value.chainId,
            usdValue = value.usdValue,
            decimals = value.decimals,
            updatedAt = value.updatedAt
        )

        dslContext.insertInto(NativeAssetUsdValueTable)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }

    override fun upsertFungibleTokenUsdValue(value: FungibleTokenUsdValue) {
        logger.info { "Upsert fungible token USD value: $value" }

        val record = FungibleTokenUsdValueRecord(
            tokenAddress = value.tokenAddress,
            chainId = value.chainId,
            usdValue = value.usdValue,
            decimals = value.decimals,
            updatedAt = value.updatedAt
        )

        dslContext.insertInto(FungibleTokenUsdValueTable)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }

    override fun upsertNftTokenUsdValue(value: NftTokenUsdValue) {
        logger.info { "Upsert NFT token USD value: $value" }

        val record = NftTokenUsdValueRecord(
            tokenAddress = value.tokenAddress,
            tokenId = value.tokenId,
            chainId = value.chainId,
            usdValue = value.usdValue,
            updatedAt = value.updatedAt
        )

        dslContext.insertInto(NftTokenUsdValueTable)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }

    override fun fetchNativeAssetValues(chainIds: List<ChainId>): Map<ChainId, NativeAssetUsdValue> {
        logger.debug { "Fetch native asset values, chainIds: $chainIds" }

        return dslContext.selectFrom(NativeAssetUsdValueTable)
            .where(NativeAssetUsdValueTable.CHAIN_ID.`in`(chainIds))
            .fetchMap(NativeAssetUsdValueTable.CHAIN_ID) {
                NativeAssetUsdValue(
                    chainId = it.chainId,
                    usdValue = it.usdValue,
                    decimals = it.decimals,
                    updatedAt = it.updatedAt
                )
            }
    }

    override fun fetchFungibleTokenValues(ids: List<Erc20TokenId>): Map<Erc20TokenId, FungibleTokenUsdValue> {
        logger.debug { "Fetch fungible token values, ids: $ids" }

        return dslContext.selectFrom(FungibleTokenUsdValueTable)
            .where(
                DSL.row(
                    FungibleTokenUsdValueTable.TOKEN_ADDRESS,
                    FungibleTokenUsdValueTable.CHAIN_ID
                ).`in`(
                    ids.map { DSL.row(it.tokenAddress, it.chainId) }
                )
            )
            .fetchMap({
                Erc20TokenId(
                    chainId = it.get(FungibleTokenUsdValueTable.CHAIN_ID),
                    tokenAddress = it.get(FungibleTokenUsdValueTable.TOKEN_ADDRESS)
                )
            }) {
                FungibleTokenUsdValue(
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    usdValue = it.usdValue,
                    decimals = it.decimals,
                    updatedAt = it.updatedAt
                )
            }
    }

    override fun fetchNftTokenValues(ids: List<NftTokenId>): Map<NftTokenId, NftTokenUsdValue> {
        logger.debug { "Fetch NFT token values, ids: $ids" }

        return dslContext.selectFrom(NftTokenUsdValueTable)
            .where(
                DSL.row(
                    NftTokenUsdValueTable.TOKEN_ADDRESS,
                    NftTokenUsdValueTable.TOKEN_ID,
                    NftTokenUsdValueTable.CHAIN_ID
                ).`in`(
                    ids.map { DSL.row(it.tokenAddress, it.tokenId, it.chainId) }
                )
            )
            .fetchMap({
                NftTokenId(
                    tokenAddress = it.get(NftTokenUsdValueTable.TOKEN_ADDRESS),
                    tokenId = it.get(NftTokenUsdValueTable.TOKEN_ID),
                    chainId = it.get(NftTokenUsdValueTable.CHAIN_ID)
                )
            }) {
                NftTokenUsdValue(
                    tokenAddress = it.tokenAddress,
                    tokenId = it.tokenId,
                    chainId = it.chainId,
                    usdValue = it.usdValue,
                    updatedAt = it.updatedAt
                )
            }
    }
}
