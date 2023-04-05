package polyflow.features.user.model.params

import polyflow.generated.jooq.enums.UserAccountType
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

data class CreateUserParams(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val accountType: UserAccountType,
    val registeredAt: UtcDateTime
)
