package polyflow.features.alias.model.request

import polyflow.config.validation.MaxStringSize
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateUserIdAliasRequest(
    @field:NotNull
    @field:MaxStringSize
    val userId: String,

    @field:NotNull
    @field:NotBlank
    @field:MaxStringSize
    val alias: String
)
