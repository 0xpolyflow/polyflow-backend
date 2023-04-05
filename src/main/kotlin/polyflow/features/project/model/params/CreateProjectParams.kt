package polyflow.features.project.model.params

import polyflow.generated.jooq.id.ProjectFeaturesId
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

data class CreateProjectParams(
    val id: ProjectId,
    val name: String,
    val ownerId: UserId,
    val createdAt: UtcDateTime,
    val featuresId: ProjectFeaturesId
)
