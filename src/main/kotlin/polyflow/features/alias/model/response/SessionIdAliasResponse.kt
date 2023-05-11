package polyflow.features.alias.model.response

import polyflow.features.alias.model.result.ValueWithAlias

data class SessionIdAliasResponse(
    val sessionId: String,
    val alias: String
) {
    constructor(valueWithAlias: ValueWithAlias<String>) : this(
        sessionId = valueWithAlias.value,
        alias = valueWithAlias.alias.value
    )
}
