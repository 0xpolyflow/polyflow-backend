package polyflow.features.events.model.response

data class EventTrackerModelEventCounts(
    val eventTracker: Array<EventCount<String>>?,
    val sessionId: Array<EventCount<String>>?,
    val utmSource: Array<EventCount<String>>?,
    val utmMedium: Array<EventCount<String>>?,
    val utmCampaign: Array<EventCount<String>>?,
    val utmContent: Array<EventCount<String>>?,
    val utmTerm: Array<EventCount<String>>?,
    val origin: Array<EventCount<String>>?,
    val path: Array<EventCount<String>>?,
    val query: Array<EventCount<String>>?,
    val referrer: Array<EventCount<String>>?
)
