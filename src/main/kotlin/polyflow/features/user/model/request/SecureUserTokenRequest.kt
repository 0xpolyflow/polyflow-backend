package polyflow.features.user.model.request

import polyflow.config.validation.MaxStringSize
import javax.validation.constraints.NotNull

data class SecureUserTokenRequest(
    @field:NotNull
    @field:MaxStringSize
    val token: String
)
