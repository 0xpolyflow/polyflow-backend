package polyflow.features.alias.model.result

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias

data class ValueWithAliases<T>(
    val value: T,
    val aliases: List<Alias>,
    val projectId: ProjectId
)
