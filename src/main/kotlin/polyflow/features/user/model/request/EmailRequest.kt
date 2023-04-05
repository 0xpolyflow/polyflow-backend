package polyflow.features.user.model.request

import polyflow.config.validation.MaxStringSize
import polyflow.config.validation.ValidEmail
import javax.validation.constraints.NotNull

data class EmailRequest(
    @field:NotNull
    @field:ValidEmail
    @field:MaxStringSize
    val email: String
)
