package polyflow.features.events.model.response

import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.WalletState
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.UtcDateTime
import java.util.UUID

data class SdkErrorEvent(
    override val id: UUID,
    override val projectId: ProjectId,
    override val createdAt: UtcDateTime,
    override val tracker: EventTrackerModel,
    val wallet: WalletState?,
    val device: DeviceState?,
    val network: NetworkState?,
    val metadata: String?
) : EventResponse
