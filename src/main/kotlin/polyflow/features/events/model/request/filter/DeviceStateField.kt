package polyflow.features.events.model.request.filter

import org.jooq.Field
import polyflow.features.events.repository.EventTable

enum class DeviceStateField(override val get: (EventTable<*, *>) -> Field<*>) : FieldGetter {
    OS({ it.os }),
    BROWSER({ it.browser }),
    COUNTRY({ it.country }),
    SCREEN({ it.screen }),
    WALLET_PROVIDER({ it.walletProvider }),
    WALLET_TYPE({ it.walletType })
}
