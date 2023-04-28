package polyflow.features.events.model

import polyflow.config.validation.MaxStringSize
import javax.validation.constraints.NotNull

data class EventTrackerModel(
    @field:NotNull
    @field:MaxStringSize
    val eventTracker: String,

    @field:NotNull
    @field:MaxStringSize
    val userId: String,

    @field:NotNull
    @field:MaxStringSize
    val sessionId: String,

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
    val path: String?,

    @field:MaxStringSize
    val query: String?
)
