package polyflow.features.portfolio.model.result

import polyflow.features.portfolio.model.json.PriceFeedJson
import polyflow.util.ChainId
import polyflow.util.ContractAddress

data class PriceFeed(
    val chainId: ChainId,
    val contractAddress: ContractAddress
) {
    constructor(json: PriceFeedJson) : this(
        chainId = ChainId(json.chainId),
        contractAddress = ContractAddress(json.contractAddress)
    )
}
