package polyflow.features.events.model.params

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Duration
import polyflow.util.UtcDateTime

data class StatisticsQuery(
    val from: UtcDateTime?,
    val to: UtcDateTime?,
    val granularity: Duration?,
    val projectId: ProjectId,
    val eventFilter: EventFilter?
)
