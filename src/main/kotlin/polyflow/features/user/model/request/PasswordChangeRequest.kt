package polyflow.features.user.model.request

import polyflow.config.validation.ValidationConstants
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class PasswordChangeRequest(
    @field:NotNull
    @field:Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.REQUEST_BODY_MAX_STRING_LENGTH)
    val oldPassword: String,
    @field:NotNull
    @field:Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.REQUEST_BODY_MAX_STRING_LENGTH)
    val newPassword: String
) {
    override fun toString(): String {
        return "PasswordChangeRequest(oldPassword=******,newPassword=******)"
    }
}
