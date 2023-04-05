package polyflow.features.project.model.result

import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

data class Project(
    val id: ProjectId,
    val name: String,
    val apiKey: String?,
    val ownerId: UserId,
    val createdAt: UtcDateTime,
    val whitelistedDomains: List<String>,
    val features: ProjectFeatures
)
