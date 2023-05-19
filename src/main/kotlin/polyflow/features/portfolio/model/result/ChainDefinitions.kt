package polyflow.features.portfolio.model.result

import polyflow.blockchain.ChainSpec
import polyflow.features.portfolio.model.json.ChainDefinitionsJson
import polyflow.util.ChainId
import polyflow.util.Decimals

data class ChainDefinitions(
    val chains: List<ChainSpec>
) {
    companion object {
        val EMPTY = ChainDefinitions(emptyList())
    }

    constructor(json: ChainDefinitionsJson) : this(
        chains = json.chains.map {
            ChainSpec(
                chainId = ChainId(it.chainId),
                rpcUrl = it.rpcUrl,
                name = it.name,
                decimals = Decimals(it.decimals),
                usdPriceFeed = PriceFeed(it.usdPriceFeed)
            )
        }
    )
}
