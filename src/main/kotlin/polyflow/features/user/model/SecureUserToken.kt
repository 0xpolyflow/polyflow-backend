package polyflow.features.user.model

import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

data class SecureUserToken(
    val token: String,
    val userId: UserId,
    val createdAt: UtcDateTime
)
