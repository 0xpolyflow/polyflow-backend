package polyflow.features.user.controller

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import polyflow.config.binding.annotation.UserBinding
import polyflow.features.user.model.request.CreateUserRequest
import polyflow.features.user.model.request.EmailRequest
import polyflow.features.user.model.request.LoginRequest
import polyflow.features.user.model.request.PasswordChangeRequest
import polyflow.features.user.model.request.SecureUserTokenRequest
import polyflow.features.user.model.request.SecureUserTokenWithPasswordRequest
import polyflow.features.user.model.response.JwtTokenResponse
import polyflow.features.user.model.result.User
import polyflow.features.user.service.UserService
import javax.validation.Valid

@Validated
@RestController
class UserController(private val userService: UserService) { // TODO test

    @PostMapping("/v1/register")
    fun register(@Valid @RequestBody requestBody: CreateUserRequest) =
        userService.create(requestBody)

    @PostMapping("/v1/register/verify")
    fun verifyRegistration(@Valid @RequestBody requestBody: SecureUserTokenRequest): ResponseEntity<JwtTokenResponse> {
        val token = userService.verify(requestBody.token)
        return ResponseEntity.ok(JwtTokenResponse(token))
    }

    @PostMapping("/v1/register/resend")
    fun resendVerificationEmail(@Valid @RequestBody requestBody: EmailRequest) =
        userService.resendVerificationEmail(requestBody.email)

    @PostMapping("/v1/login")
    fun login(@Valid @RequestBody requestBody: LoginRequest): ResponseEntity<JwtTokenResponse> {
        val token = userService.login(requestBody)
        return ResponseEntity.ok(JwtTokenResponse(token))
    }

    @PostMapping("/v1/forgot-password")
    fun sendForgotPasswordEmail(@Valid @RequestBody requestBody: EmailRequest) =
        userService.sendForgotPasswordEmail(requestBody.email)

    @PostMapping("/v1/forgot-password/reset")
    fun resetPassword(@Valid @RequestBody requestBody: SecureUserTokenWithPasswordRequest) =
        userService.resetPassword(requestBody.token, requestBody.newPassword)

    @PostMapping("/v1/change-password")
    fun changePassword(
        @UserBinding user: User,
        @Valid @RequestBody requestBody: PasswordChangeRequest
    ) = userService.changePassword(user, requestBody)
}
