package polyflow.features.user.repository

import polyflow.features.user.model.SecureUserToken
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

interface SecureUserTokenRepository {
    fun storeVerificationToken(token: SecureUserToken): SecureUserToken
    fun storePasswordResetToken(token: SecureUserToken): SecureUserToken
    fun useVerificationToken(token: String, now: UtcDateTime): UserId?
    fun usePasswordResetToken(token: String, now: UtcDateTime): UserId?
}
