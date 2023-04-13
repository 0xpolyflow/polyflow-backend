package polyflow.features.events.model.response

import polyflow.features.events.model.DeviceState
import java.time.OffsetDateTime

data class SessionEventsInfo(
    val sessionId: String,
    val totalEventCount: Int,
    val totalErrorEventCount: Int,
    val walletAddresses: Array<String>,
    val hasConnectedWallet: Boolean,
    val hasExecutedTransaction: Boolean,
    val devices: Array<DeviceState>,
    val firstEventDateTime: OffsetDateTime
)
