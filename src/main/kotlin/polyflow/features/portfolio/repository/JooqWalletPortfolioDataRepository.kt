package polyflow.features.portfolio.repository

import mu.KLogging
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import polyflow.features.portfolio.model.result.AssetBalance
import polyflow.features.portfolio.model.result.AssetRpcCall
import polyflow.features.portfolio.model.result.FungibleTokenBalance
import polyflow.features.portfolio.model.result.NftTokenBalance
import polyflow.features.portfolio.model.result.WalletPortfolioData
import polyflow.generated.jooq.tables.WalletPortfolioDataTable
import polyflow.generated.jooq.tables.records.WalletPortfolioDataRecord
import polyflow.generated.jooq.udt.records.AssetBalanceRecord
import polyflow.generated.jooq.udt.records.AssetRpcCallRecord
import polyflow.generated.jooq.udt.records.FungibleTokenBalanceRecord
import polyflow.generated.jooq.udt.records.NftTokenBalanceRecord
import polyflow.util.Balance
import polyflow.util.WalletAddress
import java.math.BigDecimal

@Repository // TODO test
class JooqWalletPortfolioDataRepository(private val dslContext: DSLContext) : WalletPortfolioDataRepository {

    companion object : KLogging()

    override fun upsertWalletPortfolioData(data: WalletPortfolioData) {
        logger.info { "Upsert wallet portfolio data: $data" }

        val record = WalletPortfolioDataRecord(
            walletAddress = data.walletAddress,
            nativeAssetBalances = data.nativeAssetBalances.map {
                AssetBalanceRecord(
                    chainId = it.chainId,
                    amount = it.amount
                )
            }.toTypedArray(),
            fungibleTokenBalances = data.fungibleTokenBalances.map {
                FungibleTokenBalanceRecord(
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    amount = it.amount
                )
            }.toTypedArray(),
            nftTokenBalances = data.nftTokenBalances.map {
                NftTokenBalanceRecord(
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    ownsAsset = it.ownsAsset,
                    ownedAssets = Array(it.amountOfOwnedAssets.rawValue.toInt()) { BigDecimal(-1) }
                    // TODO use actual owned assets
//                    ownedAssets = it.ownedAssets.map(NftId::value)
//                        .map(BigInteger::toBigDecimal)
//                        .toTypedArray()
                )
            }.toTypedArray(),
            failedRpcCalls = data.failedRpcCalls.map {
                AssetRpcCallRecord(
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    isNft = it.isNft
                )
            }.toTypedArray(),
            updatedAt = data.updatedAt
        )

        dslContext.insertInto(WalletPortfolioDataTable)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }

    override fun getWalletPortfolioData(walletAddress: WalletAddress): WalletPortfolioData? {
        logger.debug { "Fetch wallet portfolio data, walletAddress: $walletAddress" }

        return dslContext.selectFrom(WalletPortfolioDataTable)
            .where(WalletPortfolioDataTable.WALLET_ADDRESS.eq(walletAddress))
            .fetchOne { r ->
                WalletPortfolioData(
                    walletAddress = r.walletAddress,
                    nativeAssetBalances = r.nativeAssetBalances.map { nab ->
                        AssetBalance(
                            chainId = nab.chainId!!,
                            amount = nab.amount!!
                        )
                    },
                    fungibleTokenBalances = r.fungibleTokenBalances.map { ftb ->
                        FungibleTokenBalance(
                            tokenAddress = ftb.tokenAddress!!,
                            chainId = ftb.chainId!!,
                            amount = ftb.amount!!
                        )
                    },
                    nftTokenBalances = r.nftTokenBalances.map { ntb ->
                        NftTokenBalance(
                            tokenAddress = ntb.tokenAddress!!,
                            chainId = ntb.chainId!!,
                            ownsAsset = ntb.ownsAsset!!,
                            ownedAssets = emptyList(),
                            // TODO after fetching owned assets
                            // ownedAssets = ntb.ownedAssets!!.map { bd -> NftId(bd!!.toBigInteger()) },
                            amountOfOwnedAssets = Balance(ntb.ownedAssets!!.size.toBigInteger())
                        )
                    },
                    failedRpcCalls = r.failedRpcCalls.map { frc ->
                        AssetRpcCall(
                            tokenAddress = frc.tokenAddress!!,
                            chainId = frc.chainId!!,
                            isNft = frc.isNft!!
                        )
                    },
                    updatedAt = r.updatedAt
                )
            }
    }
}
