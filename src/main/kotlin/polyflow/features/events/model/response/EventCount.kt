package polyflow.features.events.model.response

data class EventCount<T>(
    val value: T,
    val count: Int
)
