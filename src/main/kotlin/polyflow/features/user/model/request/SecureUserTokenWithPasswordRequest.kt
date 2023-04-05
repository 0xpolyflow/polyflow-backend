package polyflow.features.user.model.request

import polyflow.config.validation.MaxStringSize
import polyflow.config.validation.ValidationConstants
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class SecureUserTokenWithPasswordRequest(
    @field:NotNull
    @field:MaxStringSize
    val token: String,
    @field:NotNull
    @field:Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.REQUEST_BODY_MAX_STRING_LENGTH)
    val newPassword: String
) {
    override fun toString(): String {
        return "SecureUserTokenWithPasswordRequest(token=$token,newPassword=******)"
    }
}
