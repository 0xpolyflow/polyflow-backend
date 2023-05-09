package polyflow.features.events.model.response

data class EventCounts(
    val tracker: EventTrackerModelEventCounts,
    val device: DeviceStateEventCounts,
    val network: NetworkStateEventCounts
)
