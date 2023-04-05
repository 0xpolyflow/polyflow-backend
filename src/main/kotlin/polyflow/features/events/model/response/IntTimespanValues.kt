package polyflow.features.events.model.response

import java.time.OffsetDateTime

data class IntTimespanValues(
    val from: OffsetDateTime,
    val to: OffsetDateTime,
    val value: Int
)
