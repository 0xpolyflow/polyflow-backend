package polyflow.features.portfolio.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import polyflow.blockchain.BlockchainService
import polyflow.blockchain.ChainSpec
import polyflow.config.PortfolioProperties
import polyflow.features.portfolio.model.id.Erc20TokenId
import polyflow.features.portfolio.model.json.ChainDefinitionsJson
import polyflow.features.portfolio.model.json.TokenDefinitionsJson
import polyflow.features.portfolio.model.result.AssetBalance
import polyflow.features.portfolio.model.result.AssetBalanceAndValue
import polyflow.features.portfolio.model.result.ChainDefinitions
import polyflow.features.portfolio.model.result.Deployment
import polyflow.features.portfolio.model.result.FungibleTokenBalance
import polyflow.features.portfolio.model.result.FungibleTokenBalanceAndValue
import polyflow.features.portfolio.model.result.FungibleTokenUsdValue
import polyflow.features.portfolio.model.result.NativeAssetUsdValue
import polyflow.features.portfolio.model.result.NftIdAndValue
import polyflow.features.portfolio.model.result.NftTokenBalance
import polyflow.features.portfolio.model.result.NftTokenBalanceAndValue
import polyflow.features.portfolio.model.result.PriceFeed
import polyflow.features.portfolio.model.result.Token
import polyflow.features.portfolio.model.result.TokenDefinitions
import polyflow.features.portfolio.model.result.WalletPortfolioData
import polyflow.features.portfolio.model.result.WalletPortfolioDataAndValue
import polyflow.features.portfolio.repository.UsdValuesRepository
import polyflow.features.portfolio.repository.WalletPortfolioDataRepository
import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.Decimals
import polyflow.util.UsdValue
import polyflow.util.UtcDateTime
import polyflow.util.UtcDateTimeProvider
import polyflow.util.WalletAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.time.Duration as JavaDuration

@Service // TODO test
class PortfolioServiceImpl(
    private val blockchainService: BlockchainService,
    private val walletPortfolioDataRepository: WalletPortfolioDataRepository,
    private val usdValuesRepository: UsdValuesRepository,
    private val utcDateTimeProvider: UtcDateTimeProvider,
    private val portfolioProperties: PortfolioProperties,
    objectMapper: ObjectMapper
) : PortfolioService, DisposableBean {

    companion object : KLogging() {
        private data class Price(
            val name: String,
            val price: UsdValue,
            val decimals: Decimals
        )

        private val EMPTY_PRICE = Price("", UsdValue.ZERO, Decimals.ZERO)
    }

    private val currentlyFetchingPortfolios: MutableSet<WalletAddress> = ConcurrentHashMap.newKeySet()

    private val chainDefinitions: ChainDefinitions =
        portfolioProperties.chainDefinitionsFile?.let {
            ChainDefinitions(objectMapper.readValue(it.toFile(), ChainDefinitionsJson::class.java))
        } ?: ChainDefinitions.EMPTY

    private val tokenDefinitions: TokenDefinitions = portfolioProperties.tokenDefinitionsFile?.let {
        TokenDefinitions(objectMapper.readValue(it.toFile(), TokenDefinitionsJson::class.java))
    } ?: TokenDefinitions.EMPTY

    private val executorService = Executors.newCachedThreadPool()

    override fun destroy() {
        logger.info { "Shutting down portfolio executor service..." }
        executorService.shutdown()
    }

    override fun fetchAndStorePortfolio(walletAddress: WalletAddress, force: Boolean) {
        executorService.execute {
            if (currentlyFetchingPortfolios.add(walletAddress)) {
                try {
                    safeFetchAndStorePortfolio(walletAddress, force)
                } catch (ex: Exception) {
                    logger.warn(ex) { "Portfolio fetch failed for $walletAddress" }
                } finally {
                    currentlyFetchingPortfolios.remove(walletAddress)
                }
            } else logger.debug { "Portfolio fetch already in progress for $walletAddress" }
        }
    }

    override fun fetchPortfolio(walletAddress: WalletAddress): WalletPortfolioDataAndValue? {
        logger.debug { "Fetch wallet portfolio data and value, walletAddress: $walletAddress" }

        return walletPortfolioDataRepository.getWalletPortfolioData(walletAddress)?.let { portfolio ->
            val now = utcDateTimeProvider.getUtcDateTime()
            val ownedNativeAssets = portfolio.nativeAssetBalances.filter { it.amount.isPositive() }
            val nativeAssetPrices = fetchNativeAssetPrices(ownedNativeAssets.map { it.chainId }, now)
            val nativeAssetBalances = ownedNativeAssets.map {
                val price = nativeAssetPrices[it.chainId] ?: EMPTY_PRICE

                AssetBalanceAndValue(
                    name = price.name,
                    chainId = it.chainId,
                    amount = it.amount,
                    value = it.amount.withDecimals(price.decimals) * price.price
                )
            }

            val ownedErc20Assets = portfolio.fungibleTokenBalances.filter { it.amount.isPositive() }
            val erc20AssetPrices = fetchErc20AssetPrices(
                ownedErc20Assets.map { Erc20TokenId(it.chainId, it.tokenAddress) }, now
            )
            val erc20Balances = ownedErc20Assets.map {
                val id = Erc20TokenId(it.chainId, it.tokenAddress)
                val price = erc20AssetPrices[id] ?: EMPTY_PRICE

                FungibleTokenBalanceAndValue(
                    name = price.name,
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    amount = it.amount,
                    value = it.amount.withDecimals(price.decimals) * price.price
                )
            }

            val ownedErc721Assets = portfolio.nftTokenBalances.filter { it.ownsAsset }
            val erc721AssetPrices = fetchErc721AssetPrices(
                ownedErc721Assets.map { Erc20TokenId(it.chainId, it.tokenAddress) }, now
            )
            // TODO fetch NFT values after implementation of NFT ownership check
            val erc721Balances = ownedErc721Assets.map {
                val id = Erc20TokenId(it.chainId, it.tokenAddress)
                val price = erc721AssetPrices[id] ?: EMPTY_PRICE
                val ownedAssets = it.ownedAssets.map { nftId ->
                    NftIdAndValue(
                        id = nftId,
                        value = UsdValue.ZERO // TODO calculate NFT value
                    )
                }

                NftTokenBalanceAndValue(
                    name = price.name,
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    ownsAsset = it.ownsAsset,
                    ownedAssets = ownedAssets,
                    totalValue = UsdValue(ownedAssets.sumOf { v -> v.value.value })
                )
            }

            val totalValue = nativeAssetBalances.sumOf { it.value.value } +
                erc20Balances.sumOf { it.value.value } +
                erc721Balances.sumOf { it.totalValue.value }

            WalletPortfolioDataAndValue(
                walletAddress = walletAddress,
                nativeAssetBalances = nativeAssetBalances,
                fungibleTokenBalances = erc20Balances,
                nftTokenBalances = erc721Balances,
                failedRpcCalls = portfolio.failedRpcCalls,
                totalValue = UsdValue(totalValue),
                updatedAt = portfolio.updatedAt
            )
        }
    }

    private fun safeFetchAndStorePortfolio(walletAddress: WalletAddress, force: Boolean) {
        logger.debug { "Checking for wallet portfolio data, walletAddress: $walletAddress, force: $force" }

        val currentPortfolioData = walletPortfolioDataRepository.getWalletPortfolioData(walletAddress)
        val now = utcDateTimeProvider.getUtcDateTime()
        val needsRefresh = force || currentPortfolioData == null || currentPortfolioData.updatedAt.olderThan(
            portfolioProperties.balanceRefreshInterval, now
        )

        if (needsRefresh) fetchAndUpdateBalances(walletAddress, now)
        else logger.debug { "No need to update balances for $walletAddress" }
    }

    private fun fetchAndUpdateBalances(walletAddress: WalletAddress, now: UtcDateTime) {
        logger.info { "Fetching portfolio balances for $walletAddress" }

        val nativeAssetBalances = chainDefinitions.chains.associateBy({ it.chainId }) {
            blockchainService.fetchAccountBalance(it, walletAddress)
        }

        val chainsById = chainDefinitions.chains.associateBy { it.chainId }
        val erc20TokenBalances = tokenDefinitions.erc20Tokens.fetchBalances(chainsById, walletAddress)
        val erc721TokenBalances = tokenDefinitions.erc721Tokens.fetchBalances(chainsById, walletAddress)

        val walletPortfolioData = WalletPortfolioData(
            walletAddress = walletAddress,
            nativeAssetBalances = nativeAssetBalances.map { AssetBalance(it.key, it.value.amount) },
            fungibleTokenBalances = erc20TokenBalances.flatMap {
                it.value.map { t ->
                    FungibleTokenBalance(
                        tokenAddress = it.key,
                        chainId = t.first,
                        amount = t.second.amount
                    )
                }
            },
            nftTokenBalances = erc721TokenBalances.flatMap {
                it.value.map { t ->
                    NftTokenBalance(
                        tokenAddress = it.key,
                        chainId = t.first,
                        ownsAsset = t.second.amount.isPositive(),
                        ownedAssets = emptyList() // TODO fetch owned NFTs
                    )
                }
            },
            failedRpcCalls = emptyList(), // TODO in the future...
            updatedAt = now
        )

        walletPortfolioDataRepository.upsertWalletPortfolioData(walletPortfolioData)
    }

    private fun <T : Token> List<T>.fetchBalances(chainsById: Map<ChainId, ChainSpec>, walletAddress: WalletAddress) =
        this.flatMap {
            it.deployments.mapNotNull { d -> chainsById[d.chainId]?.let { cs -> Pair(cs, d.address) } }
        }
            .groupBy(Pair<ChainSpec, *>::first) { it.second }
            .filter { it.value.isNotEmpty() }
            .flatMap {
                val balances = blockchainService.fetchErc20OrErc721AccountBalances(it.key, it.value, walletAddress)
                    .toList()

                balances.map { b -> Triple(it.key.chainId, b.first, b.second) }
            }
            .groupBy(Triple<*, ContractAddress, *>::second) { Pair(it.first, it.third) }

    private fun fetchNativeAssetPrices(
        chains: List<ChainId>,
        now: UtcDateTime
    ): Map<ChainId, Price> {
        val upToDatePrices = usdValuesRepository.fetchNativeAssetValues(chains).filterNot {
            it.value.updatedAt.olderThan(portfolioProperties.balanceRefreshInterval, now)
        }

        val missingPrices = chains.toSet() - upToDatePrices.keys
        val refreshedPrices = missingPrices.associateBy({ it }) { chainId ->
            chainDefinitions.chains.find { cs -> cs.chainId == chainId }?.let { nativeAssetSpec ->
                val priceFeed = nativeAssetSpec.usdPriceFeed
                val decimals = nativeAssetSpec.decimals

                chainDefinitions.chains.find { cs -> cs.chainId == priceFeed.chainId }?.let { chainSpec ->
                    blockchainService.fetchCurrentUsdPrice(
                        chainSpec = chainSpec,
                        priceFeedContract = priceFeed.contractAddress
                    )
                }?.let { u -> Pair(u, decimals) }
            } ?: Pair(UsdValue.ZERO, Decimals.ZERO)
        }

        refreshedPrices.forEach {
            usdValuesRepository.upsertNativeAssetUsdValue(
                NativeAssetUsdValue(
                    chainId = it.key,
                    usdValue = it.value.first,
                    decimals = it.value.second,
                    updatedAt = now
                )
            )
        }

        val prices = upToDatePrices.mapValues { Pair(it.value.usdValue, it.value.decimals) } + refreshedPrices
        val pricesWithName = prices.mapValues {
            val name = chainDefinitions.chains.find { c -> c.chainId == it.key }?.name ?: ""
            Price(
                name = name,
                price = it.value.first,
                decimals = it.value.second
            )
        }

        return pricesWithName
    }

    private fun fetchErc20AssetPrices(
        tokens: List<Erc20TokenId>,
        now: UtcDateTime
    ): Map<Erc20TokenId, Price> {
        val upToDatePrices = usdValuesRepository.fetchFungibleTokenValues(tokens).filterNot {
            it.value.updatedAt.olderThan(portfolioProperties.balanceRefreshInterval, now)
        }

        data class TokenInfo(
            val id: Erc20TokenId,
            val name: String,
            val decimals: Decimals
        )

        val missingPrices = tokens.toSet() - upToDatePrices.keys
        val missingPriceFeeds = missingPrices.mapNotNull {
            tokenDefinitions.erc20Tokens.find { erc20 ->
                erc20.deployments.contains(Deployment(it.tokenAddress, it.chainId))
            }?.let { token -> Pair(token.usdPriceFeed, TokenInfo(it, token.name, token.decimals)) }
        }.groupBy(Pair<PriceFeed, *>::first, Pair<*, TokenInfo>::second)

        val refreshedPrices = missingPriceFeeds.flatMap {
            val priceFeed = it.key
            val price = chainDefinitions.chains.find { cs -> cs.chainId == priceFeed.chainId }?.let { chainSpec ->
                blockchainService.fetchCurrentUsdPrice(
                    chainSpec = chainSpec,
                    priceFeedContract = priceFeed.contractAddress
                )
            } ?: UsdValue.ZERO

            it.value.map { tokenInfo ->
                Pair(tokenInfo.id, Price(tokenInfo.name, price, tokenInfo.decimals))
            }
        }.toMap()

        refreshedPrices.forEach {
            usdValuesRepository.upsertFungibleTokenUsdValue(
                FungibleTokenUsdValue(
                    tokenAddress = it.key.tokenAddress,
                    chainId = it.key.chainId,
                    usdValue = it.value.price,
                    decimals = it.value.decimals,
                    updatedAt = now
                )
            )
        }

        val upToDatePricesWithName = upToDatePrices.mapValues {
            val name = tokenDefinitions.erc20Tokens.find { erc20 ->
                erc20.deployments.contains(Deployment(it.key.tokenAddress, it.key.chainId))
            }?.name ?: ""
            Price(
                name = name,
                price = it.value.usdValue,
                decimals = it.value.decimals
            )
        }

        return upToDatePricesWithName + refreshedPrices
    }

    private fun fetchErc721AssetPrices(
        tokens: List<Erc20TokenId>, // TODO ERC20TokenId is here on purpose for now
        now: UtcDateTime
    ): Map<Erc20TokenId, Price> {
        val emptyPricesWithNames = tokens.map {
            val name = tokenDefinitions.erc721Tokens.find { nft ->
                nft.deployments.contains(Deployment(it.tokenAddress, it.chainId))
            }?.name ?: ""

            Pair(it, Price(name, UsdValue.ZERO, Decimals.ZERO)) // TODO fetch price
        }

        return emptyPricesWithNames.toMap()
    }

    private fun UtcDateTime.olderThan(duration: JavaDuration, now: UtcDateTime) =
        value.plus(duration).isBefore(now.value)
}
