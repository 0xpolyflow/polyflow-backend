package polyflow.util

import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Uint
import java.math.BigInteger
import java.time.Instant
import java.time.Month
import java.time.OffsetDateTime
import java.time.Year
import java.time.ZoneOffset
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration
import kotlin.time.Duration as KotlinDuration

@JvmInline
value class ChainId(val value: Long)

sealed interface EthereumAddress {
    val value: Address
    val rawValue: String
        get() = value.value

    fun toContractAddress() = ContractAddress(value)
    fun toWalletAddress() = WalletAddress(value)
}

object ZeroAddress : EthereumAddress {
    override val value: Address = Address("0")
}

@JvmInline
value class ContractAddress private constructor(override val value: Address) : EthereumAddress {
    companion object {
        operator fun invoke(value: Address) = ContractAddress(value.toString())
    }

    constructor(value: String) : this(Address(value.lowercase()))
}

@JvmInline
value class WalletAddress private constructor(override val value: Address) : EthereumAddress {
    companion object {
        operator fun invoke(value: Address) = WalletAddress(value.toString())
    }

    constructor(value: String) : this(Address(value.lowercase()))
}

@JvmInline
value class Balance(val value: Uint) {
    companion object {
        val ZERO = Balance(BigInteger.ZERO)
    }

    constructor(value: BigInteger) : this(Uint(value))

    val rawValue: BigInteger
        get() = value.value
}

@JvmInline
value class BlockNumber(val value: BigInteger)

@JvmInline
value class TransactionHash private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String) = TransactionHash("0x" + value.removePrefix("0x").lowercase())
    }
}

@JvmInline
value class UtcDateTime private constructor(val value: OffsetDateTime) {
    companion object {
        private val ZONE_OFFSET = ZoneOffset.UTC
        operator fun invoke(value: OffsetDateTime) = UtcDateTime(value.withOffsetSameInstant(ZONE_OFFSET))
        fun ofInstant(instant: Instant) = UtcDateTime(
            OffsetDateTime.ofInstant(instant, ZONE_OFFSET)
        )
    }

    operator fun minus(other: UtcDateTime): ExactDuration =
        ExactDuration(JavaDuration.between(other.value, value).toKotlinDuration())

    operator fun plus(duration: ExactDuration): UtcDateTime =
        UtcDateTime(value + duration.duration.toJavaDuration())

    operator fun minus(duration: ExactDuration): UtcDateTime =
        UtcDateTime(value - duration.duration.toJavaDuration())

    operator fun minus(duration: JavaDuration): UtcDateTime =
        UtcDateTime(value - duration)

    fun atYearStart(): UtcDateTime = UtcDateTime(OffsetDateTime.parse("${value.year}-01-01T00:00:00Z"))

    fun atYearEnd(): UtcDateTime = UtcDateTime(OffsetDateTime.parse("${value.year}-12-31T23:59:59.999Z"))

    fun atMonthStart(month: Month): UtcDateTime =
        UtcDateTime(OffsetDateTime.parse("${value.year}-${month.value.toString().padStart(2, '0')}-01T00:00:00Z"))

    fun month(): Month = value.month

    fun atMonthEnd(month: Month): UtcDateTime {
        val year = value.year
        val isLeapYear = Year.isLeap(year.toLong())
        val lastDay = month.length(isLeapYear)
        val monthDigit = month.value.toString().padStart(2, '0')
        return UtcDateTime(OffsetDateTime.parse("$year-$monthDigit-${lastDay}T23:59:59.999Z"))
    }

    fun min(other: UtcDateTime): UtcDateTime =
        if (this.value.isBefore(other.value)) this else other
}

sealed interface Duration

@JvmInline
value class ExactDuration(val duration: KotlinDuration) : Duration {
    operator fun div(other: ExactDuration): Double = duration / other.duration
    operator fun times(n: Int): ExactDuration = ExactDuration(duration * n)
}

sealed interface InexactDuration : Duration {
    companion object {
        fun parse(v: String): InexactDuration? =
            when (v) {
                MonthlyDuration.show -> MonthlyDuration
                YearlyDuration.show -> YearlyDuration
                else -> null
            }
    }

    val show: String
}

object MonthlyDuration : InexactDuration {
    override val show = "MONTHLY"
}

object YearlyDuration : InexactDuration {
    override val show = "YEARLY"
}
