package polyflow.features.alias.model.params

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias

data class CreateUserIdAliasParams(
    val userId: String,
    val alias: Alias,
    val projectId: ProjectId
)
