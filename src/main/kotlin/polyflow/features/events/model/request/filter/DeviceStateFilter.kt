package polyflow.features.events.model.request.filter

import polyflow.config.validation.MaxStringSize
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class DeviceStateFilter(
    @field:MaxStringSize
    val os: String?,

    @field:MaxStringSize
    val browser: String?,

    @field:MaxStringSize
    val country: String?,

    @field:Valid
    val screen: ScreenStateFilter?,

    @field:NotNull
    @field:MaxStringSize
    val walletProvider: String?
)
