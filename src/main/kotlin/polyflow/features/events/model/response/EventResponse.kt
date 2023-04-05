package polyflow.features.events.model.response

import polyflow.features.events.model.EventTrackerModel
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.UtcDateTime
import java.util.UUID

interface EventResponse {
    val id: UUID
    val projectId: ProjectId
    val createdAt: UtcDateTime
    val tracker: EventTrackerModel
}
