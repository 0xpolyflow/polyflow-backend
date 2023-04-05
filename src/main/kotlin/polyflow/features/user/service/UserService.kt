package polyflow.features.user.service

import polyflow.config.authentication.JwtAuthToken
import polyflow.features.user.model.request.CreateUserRequest
import polyflow.features.user.model.request.LoginRequest
import polyflow.features.user.model.request.PasswordChangeRequest
import polyflow.features.user.model.result.User

interface UserService {
    fun create(request: CreateUserRequest)
    fun verify(token: String): JwtAuthToken
    fun resendVerificationEmail(email: String)
    fun login(request: LoginRequest): JwtAuthToken
    fun sendForgotPasswordEmail(email: String)
    fun resetPassword(token: String, newPassword: String)
    fun changePassword(user: User, request: PasswordChangeRequest)
}
