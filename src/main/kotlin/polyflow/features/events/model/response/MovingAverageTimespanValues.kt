package polyflow.features.events.model.response

data class MovingAverageTimespanValues(
    val movingAverages: List<AverageTimespanValues>,
    val averageValue: Double
)
