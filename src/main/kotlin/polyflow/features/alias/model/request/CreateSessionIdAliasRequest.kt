package polyflow.features.alias.model.request

import polyflow.config.validation.MaxStringSize
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateSessionIdAliasRequest(
    @field:NotNull
    @field:MaxStringSize
    val sessionId: String,

    @field:NotNull
    @field:NotBlank
    @field:MaxStringSize
    val alias: String
)
