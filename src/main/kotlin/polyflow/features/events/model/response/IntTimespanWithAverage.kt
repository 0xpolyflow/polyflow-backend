package polyflow.features.events.model.response

data class IntTimespanWithAverage(
    val values: Array<IntTimespanValues>,
    val averageValue: Double
)
