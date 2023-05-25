@file:Suppress("FunctionName", "TooManyFunctions")

package polyflow.util

import polyflow.generated.jooq.converters.converter
import java.math.BigDecimal
import java.math.BigInteger
import java.time.OffsetDateTime

fun ChainIdConverter() = converter({ it: Long -> ChainId(it) }, { it.value })

fun ContractAddressConverter() = converter({ it: String -> ContractAddress(it) }, { it.rawValue })

fun WalletAddressConverter() = converter({ it: String -> WalletAddress(it) }, { it.rawValue })

fun BalanceConverter() = converter({ it: BigInteger -> Balance(it) }, { it.rawValue })

fun BlockNumberConverter() = converter({ it: BigInteger -> BlockNumber(it) }, { it.value })

fun TransactionHashConverter() = converter({ it: String -> TransactionHash(it) }, { it.value })

fun UtcDateTimeConverter() = converter({ it: OffsetDateTime -> UtcDateTime(it) }, { it.value })

fun AliasConverter() = converter({ it: String -> Alias(it) }, { it.value })

fun NftIdConverter() = converter({ it: BigInteger -> NftId(it) }, { it.value })

fun UsdValueConverter() = converter({ it: BigDecimal -> UsdValue(it) }, { it.value })

fun EthValueConverter() = converter({ it: BigDecimal -> EthValue(it) }, { it.value })

fun AmountConverter() = converter({ it: BigInteger -> Amount(it) }, { it.value })

fun DecimalsConverter() = converter({ it: Int -> Decimals(it) }, { it.value })
