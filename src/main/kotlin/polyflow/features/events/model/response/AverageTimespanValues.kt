package polyflow.features.events.model.response

import java.time.OffsetDateTime

data class AverageTimespanValues(
    val from: OffsetDateTime,
    val to: OffsetDateTime,
    val averageValue: Double
)
