package polyflow.features.user.model.result

import polyflow.generated.jooq.enums.UserAccountType
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

data class User(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val accountType: UserAccountType,
    val registeredAt: UtcDateTime,
    val verifiedAt: UtcDateTime?,
    val stripeCustomerId: String?,
    val stripeSessionId: String?,
    val totalDomainLimit: Int,
    val totalSeatLimit: Int
)
