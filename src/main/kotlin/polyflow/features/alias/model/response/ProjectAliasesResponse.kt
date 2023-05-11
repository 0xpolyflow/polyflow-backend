package polyflow.features.alias.model.response

import polyflow.generated.jooq.id.ProjectId

data class ProjectAliasesResponse<T>(
    val values: List<T>,
    val projectId: ProjectId
)
