package polyflow.features.events.model.request.filter

import org.jooq.Field
import polyflow.features.events.repository.EventTable

enum class NetworkStateField(override val get: (EventTable<*, *>) -> Field<*>) : FieldGetter {
    CHAIN_ID({ it.chainId }),
    GAS_PRICE({ it.gasPrice }),
    BLOCK_HEIGHT({ it.blockHeight })
}
