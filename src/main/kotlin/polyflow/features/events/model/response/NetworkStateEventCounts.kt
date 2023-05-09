package polyflow.features.events.model.response

import java.math.BigInteger

data class NetworkStateEventCounts(
    val chainId: Array<EventCount<Long>>?,
    val gasPrice: Array<EventCount<BigInteger>>?,
    val blockHeight: Array<EventCount<BigInteger>>?
)
