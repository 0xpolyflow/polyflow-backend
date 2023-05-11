package polyflow.features.alias.model.response

import polyflow.features.alias.model.result.ValueWithAlias
import polyflow.util.WalletAddress

data class WalletAddressAliasResponse(
    val walletAddress: String,
    val alias: String
) {
    constructor(valueWithAlias: ValueWithAlias<WalletAddress>) : this(
        walletAddress = valueWithAlias.value.rawValue,
        alias = valueWithAlias.alias.value
    )
}
