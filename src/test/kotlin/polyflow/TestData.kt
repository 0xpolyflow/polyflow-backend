package polyflow

import polyflow.util.ChainId
import polyflow.util.UtcDateTime
import java.time.OffsetDateTime

object TestData {
    val CHAIN_ID = ChainId(31337L)
    val TIMESTAMP: UtcDateTime = UtcDateTime(OffsetDateTime.parse("2022-02-02T00:00:00Z"))
}
