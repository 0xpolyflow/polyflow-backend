package polyflow.features.events.model.request.filter

import org.jooq.Field
import polyflow.features.events.repository.EventTable

enum class EventTrackerModelField(override val get: (EventTable<*, *>) -> Field<out String?>) : FieldGetter {
    EVENT_TRACKER({ it.eventTracker }),
    SESSION_ID({ it.sessionId }),
    UTM_SOURCE({ it.utmSource }),
    UTM_MEDIUM({ it.utmMedium }),
    UTM_CAMPAIGN({ it.utmCampaign }),
    UTM_CONTENT({ it.utmContent }),
    UTM_TERM({ it.utmTerm }),
    ORIGIN({ it.origin }),
    PATH({ it.path }),
    QUERY({ it.query }),
    REFERRER({ it.referrer })
}
