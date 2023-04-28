package polyflow.features.events.model.response

data class EventTrackerModelUniqueValues(
    val eventTracker: Array<String>?,
    val sessionId: Array<String>?,
    val utmSource: Array<String>?,
    val utmMedium: Array<String>?,
    val utmCampaign: Array<String>?,
    val utmContent: Array<String>?,
    val utmTerm: Array<String>?,
    val origin: Array<String>?,
    val path: Array<String>?,
    val query: Array<String>?
)
