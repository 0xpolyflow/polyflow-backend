package polyflow.features.portfolio.model.result

import polyflow.features.portfolio.model.json.Erc20TokenJson
import polyflow.features.portfolio.model.json.Erc721TokenJson
import polyflow.features.portfolio.model.json.TokenDefinitionsJson
import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.Decimals

data class TokenDefinitions(
    val erc20Tokens: List<Erc20Token>,
    val erc721Tokens: List<Erc721Token>
) {
    companion object {
        val EMPTY = TokenDefinitions(emptyList(), emptyList())
    }

    constructor(json: TokenDefinitionsJson) : this(
        erc20Tokens = json.erc20Tokens.map(::Erc20Token),
        erc721Tokens = json.erc721Tokens.map(::Erc721Token)
    )
}

data class Deployment(
    val address: ContractAddress,
    val chainId: ChainId
)

sealed interface Token {
    val deployments: List<Deployment>
}

data class Erc20Token(
    override val deployments: List<Deployment>,
    val name: String,
    val usdPriceFeed: PriceFeed,
    val decimals: Decimals
) : Token {
    constructor(json: Erc20TokenJson) : this(
        deployments = json.deployments.map {
            Deployment(
                address = ContractAddress(it.address),
                chainId = ChainId(it.chainId),
            )
        },
        name = json.name,
        usdPriceFeed = PriceFeed(json.usdPriceFeed),
        decimals = Decimals(json.decimals)
    )
}

data class Erc721Token(
    override val deployments: List<Deployment>,
    val name: String,
    val ethPriceFeed: PriceFeed
) : Token {
    constructor(json: Erc721TokenJson) : this(
        deployments = json.deployments.map {
            Deployment(
                address = ContractAddress(it.address),
                chainId = ChainId(it.chainId),
            )
        },
        name = json.name,
        ethPriceFeed = PriceFeed(json.ethPriceFeed)
    )
}
