package polyflow.features.alias.model.response

import polyflow.features.alias.model.result.ValueWithAlias

data class UserIdAliasResponse(
    val userId: String,
    val alias: String
) {
    constructor(valueWithAlias: ValueWithAlias<String>) : this(
        userId = valueWithAlias.value,
        alias = valueWithAlias.alias.value
    )
}
