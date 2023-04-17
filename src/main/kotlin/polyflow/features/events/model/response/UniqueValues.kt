package polyflow.features.events.model.response

data class UniqueValues(
    val tracker: EventTrackerModelUniqueValues,
    val device: DeviceStateUniqueValues
)
