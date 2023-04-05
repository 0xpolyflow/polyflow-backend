package polyflow.util

import org.springframework.stereotype.Service
import polyflow.generated.jooq.id.DatabaseIdWrapper
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.UUID

interface UuidProvider {
    fun <T> getUuid(wrapper: DatabaseIdWrapper<T>): T
    fun getRawUuid(): UUID
}

@Service
class RandomUuidProvider : UuidProvider {
    override fun <T> getUuid(wrapper: DatabaseIdWrapper<T>): T = wrapper.wrap(UUID.randomUUID())
    override fun getRawUuid(): UUID = UUID.randomUUID()
}

interface UtcDateTimeProvider {
    fun getUtcDateTime(): UtcDateTime
}

@Service
class CurrentUtcDateTimeProvider : UtcDateTimeProvider {
    override fun getUtcDateTime(): UtcDateTime = UtcDateTime(OffsetDateTime.now())
}

interface RandomProvider {
    fun getBytes(length: Int): ByteArray
}

@Service
class SecureRandomProvider : RandomProvider {

    private val secureRandom = SecureRandom()

    override fun getBytes(length: Int): ByteArray {
        return ByteArray(length).apply { secureRandom.nextBytes(this) }
    }
}
