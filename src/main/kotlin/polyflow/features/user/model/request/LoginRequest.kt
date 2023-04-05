package polyflow.features.user.model.request

import polyflow.config.validation.MaxStringSize
import polyflow.config.validation.ValidEmail
import polyflow.config.validation.ValidationConstants
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class LoginRequest(
    @field:NotNull
    @field:ValidEmail
    @field:MaxStringSize
    val email: String,
    @field:NotNull
    @field:Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.REQUEST_BODY_MAX_STRING_LENGTH)
    val password: String
) {
    override fun toString(): String {
        return "LoginRequest(email=$email,password=******)"
    }
}
