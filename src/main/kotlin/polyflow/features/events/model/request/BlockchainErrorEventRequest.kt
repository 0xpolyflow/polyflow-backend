package polyflow.features.events.model.request

import polyflow.config.validation.MaxListSize
import polyflow.config.validation.MaxStringSize
import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.TxData
import polyflow.features.events.model.WalletState
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class BlockchainErrorEventRequest(
    @field:Valid
    @field:NotNull
    override val tracker: EventTrackerModel,

    @field:Valid
    @field:NotNull
    @field:MaxListSize
    val errors: List<@NotNull @MaxStringSize String>,

    @field:Valid
    @field:NotNull
    val wallet: WalletState,

    @field:Valid
    @field:NotNull
    val device: DeviceState,

    @field:Valid
    @field:NotNull
    val network: NetworkState,

    @field:Valid
    @field:NotNull
    val tx: TxData
) : EventRequest
