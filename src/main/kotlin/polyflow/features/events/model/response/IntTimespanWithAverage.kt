package polyflow.features.events.model.response

data class IntTimespanWithAverage(
    val values: List<IntTimespanValues>,
    val averageValue: Double
)
