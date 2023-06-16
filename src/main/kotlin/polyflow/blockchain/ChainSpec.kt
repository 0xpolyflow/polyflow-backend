package polyflow.blockchain

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import polyflow.config.ApplicationProperties
import polyflow.features.portfolio.model.result.PriceFeed
import polyflow.util.ChainId
import polyflow.util.Decimals

data class ChainSpec(
    val chainId: ChainId,
    val rpcUrl: String,
    val name: String,
    val symbol: String,
    val decimals: Decimals,
    val usdPriceFeed: PriceFeed
) {
    fun web3j(applicationProperties: ApplicationProperties): Web3j =
        applicationProperties.chain[chainId]?.let {
            Web3j.build(HttpService(rpcUrl.replace("{rpcKey}", it.rpcKey)))
        } ?: Web3j.build(HttpService(rpcUrl))
}
