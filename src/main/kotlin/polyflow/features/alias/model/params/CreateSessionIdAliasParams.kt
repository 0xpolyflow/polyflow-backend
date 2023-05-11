package polyflow.features.alias.model.params

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias

data class CreateSessionIdAliasParams(
    val sessionId: String,
    val alias: Alias,
    val projectId: ProjectId
)
