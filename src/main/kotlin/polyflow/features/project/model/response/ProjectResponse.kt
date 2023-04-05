package polyflow.features.project.model.response

import polyflow.features.project.model.result.Project
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import java.time.OffsetDateTime

data class ProjectResponse(
    val id: ProjectId,
    val name: String,
    val apiKey: String?,
    val ownerId: UserId,
    val createdAt: OffsetDateTime,
    val whitelistedDomains: List<String>,
    val features: ProjectFeaturesResponse
) {
    constructor(project: Project) : this(
        id = project.id,
        name = project.name,
        apiKey = project.apiKey,
        ownerId = project.ownerId,
        createdAt = project.createdAt.value,
        whitelistedDomains = project.whitelistedDomains,
        features = ProjectFeaturesResponse(project.features)
    )
}
