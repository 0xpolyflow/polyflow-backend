package polyflow.features.portfolio.model.json

data class TokenDefinitionsJson(
    val erc20Tokens: List<Erc20TokenJson>,
    val erc721Tokens: List<Erc721TokenJson>
)

data class DeploymentJson(
    val address: String,
    val chainId: Long
)

data class Erc20TokenJson(
    val deployments: List<DeploymentJson>,
    val name: String,
    val usdPriceFeed: PriceFeedJson,
    val decimals: Int
)

data class Erc721TokenJson(
    val deployments: List<DeploymentJson>,
    val name: String,
    val ethPriceFeed: PriceFeedJson
)
