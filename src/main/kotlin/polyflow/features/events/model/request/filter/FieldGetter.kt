package polyflow.features.events.model.request.filter

import org.jooq.Field
import polyflow.features.events.repository.EventTable

interface FieldGetter {
    val get: (EventTable<*, *>) -> Field<*>
}
