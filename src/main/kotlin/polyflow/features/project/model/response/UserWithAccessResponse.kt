package polyflow.features.project.model.response

import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.UserId

data class UserWithAccessResponse(
    val userId: UserId,
    val email: String,
    val accessType: AccessType
)
