package polyflow.features.user.service

import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import polyflow.config.EmailProperties
import polyflow.config.JwtProperties
import polyflow.config.authentication.JwtAuthToken
import polyflow.config.authentication.JwtTokenUtils
import polyflow.exception.AccessForbiddenException
import polyflow.exception.InvalidUserSecureTokenException
import polyflow.exception.UserNotYetVerifiedException
import polyflow.features.email.model.Email
import polyflow.features.email.service.EmailSenderService
import polyflow.features.user.model.SecureUserToken
import polyflow.features.user.model.params.CreateUserParams
import polyflow.features.user.model.request.CreateUserRequest
import polyflow.features.user.model.request.LoginRequest
import polyflow.features.user.model.request.PasswordChangeRequest
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.SecureUserTokenRepository
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.enums.UserAccountType
import polyflow.generated.jooq.id.UserId
import polyflow.util.RandomProvider
import polyflow.util.UtcDateTimeProvider
import polyflow.util.UuidProvider
import java.util.Base64
import kotlin.time.toKotlinDuration

@Service
class UserServiceImpl(
    private val emailSenderService: EmailSenderService,
    private val userRepository: UserRepository,
    private val secureUserTokenRepository: SecureUserTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val uuidProvider: UuidProvider,
    private val utcDateTimeProvider: UtcDateTimeProvider,
    private val randomProvider: RandomProvider,
    private val jwtProperties: JwtProperties,
    private val emailProperties: EmailProperties
) : UserService { // TODO test

    companion object : KLogging() {
        private val ENCODER = Base64.getUrlEncoder()
        private const val TOKEN_BYTES = 256
        private const val INVALID_USER = "Invalid email or password"
        private const val INVALID_PASSWORD = "Invalid password"
    }

    override fun create(request: CreateUserRequest) {
        logger.info { "Create user: $request" }

        val params = CreateUserParams(
            id = uuidProvider.getUuid(UserId),
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            accountType = UserAccountType.EMAIL_REGISTERED,
            registeredAt = utcDateTimeProvider.getUtcDateTime()
        )

        val user = userRepository.store(params)
        val verificationToken = user.generateToken()

        secureUserTokenRepository.storeVerificationToken(verificationToken)

        verificationToken.sendVerificationEmail(user.email)
    }

    override fun verify(token: String): JwtAuthToken {
        logger.info { "Verify user by token: $token" }

        val now = utcDateTimeProvider.getUtcDateTime()

        val userId = secureUserTokenRepository.useVerificationToken(token, now)
            ?: throw InvalidUserSecureTokenException()

        userRepository.setVerifiedAt(userId, now)

        val user = userRepository.getById(userId)!!

        return JwtTokenUtils.encodeToken(
            id = user.id,
            email = user.email,
            privateKey = jwtProperties.privateKey,
            tokenValidity = jwtProperties.tokenValidity.toKotlinDuration()
        )
    }

    override fun resendVerificationEmail(email: String) {
        logger.info { "Resend verification email: $email" }

        userRepository.getByEmail(email)?.let { user ->
            val verificationToken = user.generateToken()

            secureUserTokenRepository.storeVerificationToken(verificationToken)

            verificationToken.sendVerificationEmail(email)
        }
    }

    override fun login(request: LoginRequest): JwtAuthToken {
        logger.info { "Login user: $request" }

        val user = userRepository.getByEmail(request.email) ?: throw AccessForbiddenException(INVALID_USER)

        if (user.verifiedAt == null) {
            throw UserNotYetVerifiedException()
        }

        if (passwordEncoder.matches(request.password, user.passwordHash).not()) {
            throw AccessForbiddenException(INVALID_USER)
        }

        return JwtTokenUtils.encodeToken(
            id = user.id,
            email = user.email,
            privateKey = jwtProperties.privateKey,
            tokenValidity = jwtProperties.tokenValidity.toKotlinDuration()
        )
    }

    override fun sendForgotPasswordEmail(email: String) {
        logger.info { "Send user forgot password email: $email" }

        userRepository.getByEmail(email)?.let { user ->
            val passwordResetToken = user.generateToken()

            secureUserTokenRepository.storePasswordResetToken(passwordResetToken)

            passwordResetToken.sendForgotPasswordEmail(email)
        }
    }

    override fun resetPassword(token: String, newPassword: String) {
        logger.info { "Reset password by token: $token" }

        val now = utcDateTimeProvider.getUtcDateTime()

        val userId = secureUserTokenRepository.usePasswordResetToken(token, now)
            ?: throw InvalidUserSecureTokenException()

        userRepository.setPassword(userId, passwordEncoder.encode(newPassword))
    }

    override fun changePassword(user: User, request: PasswordChangeRequest) {
        logger.info { "Change user password: $user" }

        if (passwordEncoder.matches(request.oldPassword, user.passwordHash).not()) {
            throw AccessForbiddenException(INVALID_PASSWORD)
        }

        userRepository.setPassword(user.id, passwordEncoder.encode(request.newPassword))
    }

    private fun User.generateToken() =
        SecureUserToken(
            token = ENCODER.encodeToString(randomProvider.getBytes(TOKEN_BYTES)),
            userId = id,
            createdAt = utcDateTimeProvider.getUtcDateTime()
        )

    private fun SecureUserToken.sendVerificationEmail(email: String) {
        emailSenderService.sendEmail(
            Email(
                recipient = email,
                subject = emailProperties.verificationEmailSubject,
                messageBody = emailProperties.verificationEmailTemplate.replace("{token}", this.token)
            )
        )
    }

    private fun SecureUserToken.sendForgotPasswordEmail(email: String) {
        emailSenderService.sendEmail(
            Email(
                recipient = email,
                subject = emailProperties.passwordResetEmailSubject,
                messageBody = emailProperties.passwordResetEmailTemplate.replace("{token}", this.token)
            )
        )
    }
}
