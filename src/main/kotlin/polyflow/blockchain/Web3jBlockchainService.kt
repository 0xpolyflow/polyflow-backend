package polyflow.blockchain

import mu.KLogging
import org.springframework.stereotype.Service
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.core.BatchRequest
import org.web3j.protocol.core.BatchResponse
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import polyflow.config.ApplicationProperties
import polyflow.exception.BlockchainReadException
import polyflow.features.portfolio.model.result.AssetRpcCall
import polyflow.util.AccountBalance
import polyflow.util.Balance
import polyflow.util.BlockParameter
import polyflow.util.ContractAddress
import polyflow.util.EthValue
import polyflow.util.UsdValue
import polyflow.util.WalletAddress
import java.math.BigDecimal

@Service // TODO test
class Web3jBlockchainService(
    private val applicationProperties: ApplicationProperties
) : BlockchainService {

    companion object : KLogging() {
        private const val BALANCE_OF = "balanceOf"
        private const val DECIMALS = "decimals"
        private const val LATEST_ANSWER = "latestAnswer"
        private val BALANCE_OF_RETURN_TYPE = listOf(object : TypeReference<Uint256>() {})
        private val DECIMALS_RETURN_TYPE = listOf(object : TypeReference<Uint8>() {})
        private val LATEST_ANSWER_RETURN_TYPE = listOf(object : TypeReference<Int256>() {})
    }

    override fun fetchAccountBalance(
        chainSpec: ChainSpec,
        walletAddress: WalletAddress,
        blockParameter: BlockParameter
    ): AccountBalance {
        logger.debug {
            "Fetching account balance, chainSpec: $chainSpec, walletAddress: $walletAddress," +
                " blockParameter: $blockParameter"
        }

        val web3j = chainSpec.web3j(applicationProperties)

        val balance = web3j.ethGetBalance(
            walletAddress.rawValue,
            blockParameter.toWeb3Parameter()
        ).sendSafely()?.balance?.let { Balance(it) }
            ?: throw BlockchainReadException("Unable to read balance of address: ${walletAddress.rawValue}")

        return AccountBalance(
            wallet = walletAddress,
            amount = balance
        )
    }

    override fun fetchErc20OrErc721AccountBalances(
        chainSpec: ChainSpec,
        contractAddresses: List<ContractAddressAndType>,
        walletAddress: WalletAddress,
        blockParameter: BlockParameter
    ): BalancesAndFailedRpcCalls {
        logger.debug {
            "Fetching ERC20/ERC721 balance, chainSpec: $chainSpec, contractAddresses: $contractAddresses," +
                " walletAddress: $walletAddress, blockParameter: $blockParameter"
        }

        val function = Function(
            BALANCE_OF,
            listOf(walletAddress.value),
            BALANCE_OF_RETURN_TYPE
        )
        val data = FunctionEncoder.encode(function)

        val web3j = chainSpec.web3j(applicationProperties)
        val batch = web3j.newBatch()

        contractAddresses.forEach {
            val address = it.contractAddress.rawValue

            batch.add(
                web3j.ethCall(
                    Transaction.createEthCallTransaction(address, address, data),
                    blockParameter.toWeb3Parameter()
                )
            )
        }

        val batchResponse = batch.sendSafely()
            ?: throw BlockchainReadException("Unable to execute batch call to fetch ERC20 balances")

        val responses = batchResponse.responses.map {
            if (it.hasError()) null else {
                function.tryDecode(it.result.toString())
                    ?.firstOrNull() as? Uint256
            }
        }

        val balances = contractAddresses.zip(responses).mapNotNull { it.second?.let { s -> Pair(it.first, s) } }
            .associateBy({ it.first.contractAddress }) {
                AccountBalance(
                    wallet = walletAddress,
                    amount = Balance(it.second)
                )
            }

        val failedRpcCalls = contractAddresses.zip(responses).filter { it.second == null }
            .map {
                AssetRpcCall(
                    tokenAddress = it.first.contractAddress,
                    chainId = chainSpec.chainId,
                    isNft = it.first.isNft
                )
            }

        return BalancesAndFailedRpcCalls(balances, failedRpcCalls)
    }

    override fun fetchCurrentUsdPrice(
        chainSpec: ChainSpec,
        priceFeedContract: ContractAddress,
        blockParameter: BlockParameter
    ): UsdValue = UsdValue(fetchCurrentPrice(chainSpec, priceFeedContract, blockParameter, "USD"))

    override fun fetchCurrentEthPrice(
        chainSpec: ChainSpec,
        priceFeedContract: ContractAddress,
        blockParameter: BlockParameter
    ): EthValue = EthValue(fetchCurrentPrice(chainSpec, priceFeedContract, blockParameter, "ETH"))

    // uses ChainLink price feed contracts
    private fun fetchCurrentPrice(
        chainSpec: ChainSpec,
        priceFeedContract: ContractAddress,
        blockParameter: BlockParameter,
        currency: String
    ): BigDecimal {
        logger.debug {
            "Fetching asset price in $currency, chainSpec: $chainSpec, priceFeedContract: $priceFeedContract," +
                " blockParameter: $blockParameter"
        }

        val decimalsFunction = Function(
            DECIMALS,
            emptyList(),
            DECIMALS_RETURN_TYPE
        )
        val decimalsData = FunctionEncoder.encode(decimalsFunction)

        val latestAnswerFunction = Function(
            LATEST_ANSWER,
            emptyList(),
            LATEST_ANSWER_RETURN_TYPE
        )
        val latestAnswerData = FunctionEncoder.encode(latestAnswerFunction)

        val web3j = chainSpec.web3j(applicationProperties)
        val batch = web3j.newBatch()
        val address = priceFeedContract.rawValue

        batch.add(
            web3j.ethCall(
                Transaction.createEthCallTransaction(address, address, decimalsData),
                blockParameter.toWeb3Parameter()
            )
        )
        batch.add(
            web3j.ethCall(
                Transaction.createEthCallTransaction(address, address, latestAnswerData),
                blockParameter.toWeb3Parameter()
            )
        )

        val batchResponse = batch.sendSafely()
            ?: throw BlockchainReadException("Unable to execute batch call to fetch asset $currency balances")

        val decimals = batchResponse.responses?.get(0)?.takeIf { !it.hasError() }?.let {
            decimalsFunction.tryDecode(it.result.toString())
                ?.firstOrNull() as? Uint8
        } ?: throw BlockchainReadException("Could not fetch number of decimals of price feed")

        val unscaledUsdValue = batchResponse.responses?.get(1)?.takeIf { !it.hasError() }?.let {
            latestAnswerFunction.tryDecode(it.result.toString())
                ?.firstOrNull() as? Int256
        } ?: throw BlockchainReadException("Could not fetch latest price feed")

        return unscaledUsdValue.value.toBigDecimal().movePointLeft(decimals.value.toInt())
    }

    private fun Function.tryDecode(data: String): List<Type<*>>? =
        try {
            FunctionReturnDecoder.decode(data, outputParameters)
        } catch (ex: Exception) {
            logger.warn("Failed to decode function return value", ex)
            null
        }

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    private fun <S, T : Response<*>?> Request<S, T>.sendSafely(): T? {
        try {
            val value = this.send()
            if (value?.hasError() == true) {
                logger.warn { "Web3j call errors: ${value.error.message}" }
                return null
            }
            return value
        } catch (ex: Exception) {
            logger.warn("Failed blockchain call", ex)
            return null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun BatchRequest.sendSafely(): BatchResponse? =
        try {
            this.send()
        } catch (ex: Exception) {
            logger.warn("Failed batch call call", ex)
            null
        }
}
