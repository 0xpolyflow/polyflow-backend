package polyflow.features.events.model

import polyflow.config.validation.MaxStringSize
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class DeviceState(
    @field:MaxStringSize
    val os: String?,

    @field:MaxStringSize
    val browser: String?,

    @field:MaxStringSize
    val country: String?,

    @field:Valid
    val screen: ScreenState?,

    @field:NotNull
    @field:MaxStringSize
    val walletProvider: String,

    @field:NotNull
    @field:MaxStringSize
    val walletType: String
)
