package polyflow.features.portfolio.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import polyflow.blockchain.BlockchainService
import polyflow.blockchain.ChainSpec
import polyflow.blockchain.ContractAddressAndType
import polyflow.config.PortfolioProperties
import polyflow.features.portfolio.model.id.Erc20TokenId
import polyflow.features.portfolio.model.id.NftTokenId
import polyflow.features.portfolio.model.json.ChainDefinitionsJson
import polyflow.features.portfolio.model.json.TokenDefinitionsJson
import polyflow.features.portfolio.model.result.AssetBalance
import polyflow.features.portfolio.model.result.AssetBalanceAndValue
import polyflow.features.portfolio.model.result.AssetRpcCall
import polyflow.features.portfolio.model.result.ChainDefinitions
import polyflow.features.portfolio.model.result.Deployment
import polyflow.features.portfolio.model.result.FungibleTokenBalance
import polyflow.features.portfolio.model.result.FungibleTokenBalanceAndValue
import polyflow.features.portfolio.model.result.FungibleTokenUsdValue
import polyflow.features.portfolio.model.result.NativeAssetUsdValue
import polyflow.features.portfolio.model.result.NftTokenBalance
import polyflow.features.portfolio.model.result.NftTokenBalanceAndValue
import polyflow.features.portfolio.model.result.NftTokenEthValue
import polyflow.features.portfolio.model.result.PriceFeed
import polyflow.features.portfolio.model.result.Token
import polyflow.features.portfolio.model.result.TokenDefinitions
import polyflow.features.portfolio.model.result.WalletPortfolioData
import polyflow.features.portfolio.model.result.WalletPortfolioDataAndValue
import polyflow.features.portfolio.repository.UsdAndEthValuesRepository
import polyflow.features.portfolio.repository.WalletPortfolioDataRepository
import polyflow.util.AccountBalance
import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.Decimals
import polyflow.util.EthValue
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
    private val usdValuesRepository: UsdAndEthValuesRepository,
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
        private val ETH_CHAIN_ID = ChainId(1L)
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
            val nativeAssetChainIds = ownedNativeAssets.map { it.chainId }.let {
                if (it.contains(ETH_CHAIN_ID)) it else it + ETH_CHAIN_ID
            }
            val nativeAssetPrices = fetchNativeAssetPrices(nativeAssetChainIds, now)
            val nativeAssetBalances = ownedNativeAssets.map {
                val price = nativeAssetPrices[it.chainId] ?: EMPTY_PRICE
                val amountWithDecimals = it.amount.withDecimals(price.decimals)

                AssetBalanceAndValue(
                    name = price.name,
                    chainId = it.chainId,
                    amount = amountWithDecimals,
                    value = amountWithDecimals * price.price
                )
            }

            val ownedErc20Assets = portfolio.fungibleTokenBalances.filter { it.amount.isPositive() }
            val erc20AssetPrices = fetchErc20AssetPrices(
                ownedErc20Assets.map { Erc20TokenId(it.chainId, it.tokenAddress) }, now
            )
            val erc20Balances = ownedErc20Assets.map {
                val id = Erc20TokenId(it.chainId, it.tokenAddress)
                val price = erc20AssetPrices[id] ?: EMPTY_PRICE
                val amountWithDecimals = it.amount.withDecimals(price.decimals)

                FungibleTokenBalanceAndValue(
                    name = price.name,
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    amount = amountWithDecimals,
                    value = amountWithDecimals * price.price
                )
            }

            val ownedErc721Assets = portfolio.nftTokenBalances.filter { it.ownsAsset }
            val erc721AssetPrices = fetchErc721AssetPrices(
                tokens = ownedErc721Assets.map { NftTokenId(it.chainId, it.tokenAddress) },
                now = now,
                ethPrice = nativeAssetPrices[ETH_CHAIN_ID]?.price ?: UsdValue.ZERO
            )
            val erc721Balances = ownedErc721Assets.map {
                val id = NftTokenId(it.chainId, it.tokenAddress)
                val price = erc721AssetPrices[id] ?: EMPTY_PRICE

                NftTokenBalanceAndValue(
                    name = price.name,
                    tokenAddress = it.tokenAddress,
                    chainId = it.chainId,
                    ownsAsset = it.ownsAsset,
                    ownedAssets = it.ownedAssets,
                    amountOfOwnedAssets = it.amountOfOwnedAssets,
                    totalValue = it.amountOfOwnedAssets.withDecimals(price.decimals) * price.price
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
        val (erc20TokenBalances, failedErc20RpcCalls) = tokenDefinitions.erc20Tokens.fetchBalances(
            chainsById = chainsById,
            walletAddress = walletAddress,
            isNft = false
        )
        val (erc721TokenBalances, failedErc721RpcCalls) = tokenDefinitions.erc721Tokens.fetchBalances(
            chainsById = chainsById,
            walletAddress = walletAddress,
            isNft = true
        )

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
                        amountOfOwnedAssets = t.second.amount,
                        ownedAssets = emptyList() // TODO fetch owned NFTs
                    )
                }
            },
            failedRpcCalls = failedErc20RpcCalls + failedErc721RpcCalls,
            updatedAt = now
        )

        walletPortfolioDataRepository.upsertWalletPortfolioData(walletPortfolioData)
    }

    private fun <T : Token> List<T>.fetchBalances(
        chainsById: Map<ChainId, ChainSpec>,
        walletAddress: WalletAddress,
        isNft: Boolean
    ): Pair<Map<ContractAddress, List<Pair<ChainId, AccountBalance>>>, List<AssetRpcCall>> {
        data class FetchResponse(
            val chainId: ChainId,
            val contractAddress: ContractAddress,
            val accountBalance: AccountBalance,
            val failedRpcCalls: List<AssetRpcCall>
        )

        val responses = this.flatMap {
            it.deployments.mapNotNull { d -> chainsById[d.chainId]?.let { cs -> Pair(cs, d.address) } }
        }
            .groupBy(Pair<ChainSpec, *>::first) { it.second }
            .filter { it.value.isNotEmpty() }
            .flatMap {
                val balancesAndFailedRpcCalls = blockchainService.fetchErc20OrErc721AccountBalances(
                    it.key,
                    it.value.map { ca -> ContractAddressAndType(ca, isNft) },
                    walletAddress
                )

                balancesAndFailedRpcCalls.balances.toList().map { b ->
                    FetchResponse(
                        chainId = it.key.chainId,
                        contractAddress = b.first,
                        accountBalance = b.second,
                        failedRpcCalls = balancesAndFailedRpcCalls.failedRpcCalls
                    )
                }
            }

        return Pair(
            responses.groupBy(FetchResponse::contractAddress) { Pair(it.chainId, it.accountBalance) },
            responses.flatMap { it.failedRpcCalls }.distinct()
        )
    }

    private fun fetchNativeAssetPrices(
        chains: List<ChainId>,
        now: UtcDateTime
    ): Map<ChainId, Price> {
        val upToDatePrices = usdValuesRepository.fetchNativeAssetValues(chains.toList()).filterNot {
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
            val name = chainDefinitions.chains.find { c -> c.chainId == it.key }?.symbol ?: ""
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
        tokens: List<NftTokenId>,
        now: UtcDateTime,
        ethPrice: UsdValue
    ): Map<NftTokenId, Price> {
        val upToDatePrices = usdValuesRepository.fetchNftTokenValues(tokens).filterNot {
            it.value.updatedAt.olderThan(portfolioProperties.balanceRefreshInterval, now)
        }

        data class TokenInfo(
            val id: NftTokenId,
            val name: String
        )

        val missingPrices = tokens.toSet() - upToDatePrices.keys
        val missingPriceFeeds = missingPrices.mapNotNull {
            tokenDefinitions.erc721Tokens.find { erc721 ->
                erc721.deployments.contains(Deployment(it.tokenAddress, it.chainId))
            }?.let { token -> Pair(token.ethPriceFeed, TokenInfo(it, token.name)) }
        }.groupBy(Pair<PriceFeed, *>::first, Pair<*, TokenInfo>::second)

        data class EthPrice(
            val name: String,
            val price: EthValue
        )

        val refreshedPrices = missingPriceFeeds.flatMap {
            val priceFeed = it.key
            val price = chainDefinitions.chains.find { cs -> cs.chainId == priceFeed.chainId }?.let { chainSpec ->
                blockchainService.fetchCurrentEthPrice(
                    chainSpec = chainSpec,
                    priceFeedContract = priceFeed.contractAddress
                )
            } ?: EthValue.ZERO

            it.value.map { tokenInfo ->
                Pair(tokenInfo.id, EthPrice(tokenInfo.name, price))
            }
        }.toMap()

        refreshedPrices.forEach {
            usdValuesRepository.upsertNftTokenEthValue(
                NftTokenEthValue(
                    tokenAddress = it.key.tokenAddress,
                    chainId = it.key.chainId,
                    ethValue = it.value.price,
                    updatedAt = now
                )
            )
        }

        val upToDatePricesWithName = upToDatePrices.mapValues {
            val name = tokenDefinitions.erc721Tokens.find { erc721 ->
                erc721.deployments.contains(Deployment(it.key.tokenAddress, it.key.chainId))
            }?.name ?: ""
            Price(
                name = name,
                price = UsdValue(it.value.ethValue.value * ethPrice.value),
                decimals = Decimals.ZERO
            )
        }

        return upToDatePricesWithName + refreshedPrices.mapValues {
            Price(
                name = it.value.name,
                price = UsdValue(it.value.price.value * ethPrice.value),
                decimals = Decimals.ZERO
            )
        }
    }

    private fun UtcDateTime.olderThan(duration: JavaDuration, now: UtcDateTime) =
        value.plus(duration).isBefore(now.value)
}
