package polyflow.features.events.model.request

import polyflow.config.validation.MaxListSize
import polyflow.config.validation.MaxStringSize
import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.WalletState
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class ErrorEventRequest(
    @field:Valid
    @field:NotNull
    override val tracker: EventTrackerModel,

    @field:Valid
    @field:NotNull
    @field:MaxListSize
    val errors: List<@NotNull @MaxStringSize String>,

    @field:Valid
    val wallet: WalletState?,

    @field:Valid
    @field:NotNull
    val device: DeviceState,

    @field:Valid
    val network: NetworkState?
) : EventRequest
