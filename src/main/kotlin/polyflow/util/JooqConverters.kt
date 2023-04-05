@file:Suppress("FunctionName", "TooManyFunctions")

package polyflow.util

import polyflow.generated.jooq.converters.converter
import java.math.BigInteger
import java.time.OffsetDateTime

fun ChainIdConverter() = converter({ it: Long -> ChainId(it) }, { it.value })

fun ContractAddressConverter() = converter({ it: String -> ContractAddress(it) }, { it.rawValue })

fun WalletAddressConverter() = converter({ it: String -> WalletAddress(it) }, { it.rawValue })

fun BalanceConverter() = converter({ it: BigInteger -> Balance(it) }, { it.rawValue })

fun BlockNumberConverter() = converter({ it: BigInteger -> BlockNumber(it) }, { it.value })

fun TransactionHashConverter() = converter({ it: String -> TransactionHash(it) }, { it.value })

fun UtcDateTimeConverter() = converter({ it: OffsetDateTime -> UtcDateTime(it) }, { it.value })
