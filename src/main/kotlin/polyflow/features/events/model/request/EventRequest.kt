package polyflow.features.events.model.request

import polyflow.features.events.model.EventTrackerModel

interface EventRequest {
    val tracker: EventTrackerModel
}
