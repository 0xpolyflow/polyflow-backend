package polyflow.features.events.model.request.filter

import polyflow.config.validation.MaxStringSize

data class EventTrackerModelFilter(
    @field:MaxStringSize
    val eventTracker: String?,

    @field:MaxStringSize
    val userId: String?,

    @field:MaxStringSize
    val sessionId: String?,

    @field:MaxStringSize
    val utmSource: String?,

    @field:MaxStringSize
    val utmMedium: String?,

    @field:MaxStringSize
    val utmCampaign: String?,

    @field:MaxStringSize
    val utmContent: String?,

    @field:MaxStringSize
    val utmTerm: String?,

    @field:MaxStringSize
    val origin: String?,

    @field:MaxStringSize
    val path: String?
)
