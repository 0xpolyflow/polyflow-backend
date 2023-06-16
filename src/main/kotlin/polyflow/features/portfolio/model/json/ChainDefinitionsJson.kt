package polyflow.features.portfolio.model.json

data class ChainDefinitionsJson(
    val chains: List<ChainSpecJson>
)

data class ChainSpecJson(
    val chainId: Long,
    val rpcUrl: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val usdPriceFeed: PriceFeedJson
)
