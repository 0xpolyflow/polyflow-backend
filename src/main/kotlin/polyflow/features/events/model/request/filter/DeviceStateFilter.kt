package polyflow.features.events.model.request.filter

import polyflow.config.validation.MaxStringSize
import javax.validation.Valid

data class DeviceStateFilter(
    @field:MaxStringSize
    val os: String?,

    @field:MaxStringSize
    val browser: String?,

    @field:MaxStringSize
    val country: String?,

    @field:Valid
    val screen: ScreenStateFilter?,

    @field:MaxStringSize
    val walletProvider: String?,

    @field:MaxStringSize
    val walletType: String?
)
