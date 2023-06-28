package polyflow.features.events.model.request

import polyflow.config.validation.MaxMetadataStringSize
import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.WalletState
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class SdkErrorEventRequest(
    @field:Valid
    @field:NotNull
    override val tracker: EventTrackerModel,

    @field:Valid
    val wallet: WalletState?,

    @field:Valid
    val device: DeviceState?,

    @field:Valid
    val network: NetworkState?,

    @field:MaxMetadataStringSize
    val metadata: String?
) : EventRequest
