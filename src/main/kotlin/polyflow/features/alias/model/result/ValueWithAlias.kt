package polyflow.features.alias.model.result

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias

data class ValueWithAlias<T>(
    val value: T,
    val alias: Alias,
    val projectId: ProjectId
)
