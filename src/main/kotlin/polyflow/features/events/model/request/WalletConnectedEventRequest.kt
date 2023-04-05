package polyflow.features.events.model.request

import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.WalletState
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class WalletConnectedEventRequest(
    @field:Valid
    @field:NotNull
    override val tracker: EventTrackerModel,

    @field:Valid
    @field:NotNull
    val wallet: WalletState,

    @field:Valid
    @field:NotNull
    val device: DeviceState,

    @field:Valid
    @field:NotNull
    val network: NetworkState
) : EventRequest
