package polyflow.features.events.model.response

import polyflow.features.events.model.ScreenState

data class DeviceStateUniqueValues(
    val os: Array<String>?,
    val browser: Array<String>?,
    val country: Array<String>?,
    val screen: Array<ScreenState>?,
    val walletProvider: Array<String>?,
    val walletType: Array<String>?
)
