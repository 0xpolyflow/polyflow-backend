package polyflow.features.events.model.response

import polyflow.features.events.model.ScreenState

data class DeviceStateEventCounts(
    val os: Array<EventCount<String>>?,
    val browser: Array<EventCount<String>>?,
    val country: Array<EventCount<String>>?,
    val screen: Array<EventCount<ScreenState>>?,
    val walletProvider: Array<EventCount<String>>?,
    val walletType: Array<EventCount<String>>?
)
