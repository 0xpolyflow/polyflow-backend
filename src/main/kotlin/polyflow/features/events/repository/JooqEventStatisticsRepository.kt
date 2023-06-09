package polyflow.features.events.repository

import mu.KLogging
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl
import org.jooq.util.postgres.PostgresDSL
import org.springframework.stereotype.Repository
import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.ScreenState
import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.params.StatisticsQuery
import polyflow.features.events.model.request.filter.EventTrackerModelField
import polyflow.features.events.model.request.filter.Pagination
import polyflow.features.events.model.response.AverageTimespanValues
import polyflow.features.events.model.response.IntTimespanValues
import polyflow.features.events.model.response.IntTimespanWithAverage
import polyflow.features.events.model.response.MovingAverageTimespanValues
import polyflow.features.events.model.response.ProjectUserStats
import polyflow.features.events.model.response.SessionEventsInfo
import polyflow.features.events.model.response.UserEventsInfo
import polyflow.features.events.model.response.UsersWalletsAndTransactionsInfo
import polyflow.features.events.model.response.WalletConnectionsAndTransactionsInfo
import polyflow.features.events.model.response.WalletConnectionsAndTransactionsInfoForNetwork
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.tables.TxRequestEventTable
import polyflow.generated.jooq.udt.EventTrackerModel
import polyflow.generated.jooq.udt.TxData
import polyflow.generated.jooq.udt.records.DeviceStateRecord
import polyflow.util.Duration
import polyflow.util.ExactDuration
import polyflow.util.InexactDuration
import polyflow.util.MonthlyDuration
import polyflow.util.TransactionHash
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress
import polyflow.util.WithCount
import polyflow.util.YearlyDuration
import java.time.OffsetDateTime
import kotlin.math.floor
import kotlin.time.Duration.Companion.milliseconds
import polyflow.features.events.model.DeviceState as DeviceStateModel

@Repository
@Suppress("TooManyFunctions")
class JooqEventStatisticsRepository(private val dslContext: DSLContext) : EventStatisticsRepository { // TODO test

    companion object : KLogging() {
        private data class TimeRange(val start: UtcDateTime, val end: UtcDateTime, val scale: Int)
    }

    // TODO improve efficiency
    override fun totalConnectedWallets(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find total connected wallets, query: $query, pagination: $pagination" }

        return fetchUniqueWalletConnectedEvents(query)
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun totalNewWallets(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find total new wallets, query: $query, pagination: $pagination" }

        val table = EventTables.WalletConnectedTable

        val walletAddressField = table.walletAddress
        val minCreatedAtField = DSL.min(table.createdAt)

        val beforeQuery = query.copy(from = null, to = query.from)
        val previouslyConnectedWallets = dslContext.select(walletAddressField)
            .where(beforeQuery.condition(table))

        return dslContext.select(walletAddressField, minCreatedAtField)
            .from(table.db)
            .where(
                DSL.and(
                    query.condition(table),
                    walletAddressField.notIn(previouslyConnectedWallets)
                )
            )
            .groupBy(table.walletAddress)
            .orderBy(minCreatedAtField)
            .fetch { Pair(it.component1(), it.component2()) }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun periodActiveWallets(query: StatisticsQuery, pagination: Pagination): IntTimespanWithAverage {
        logger.debug { "Find period active wallets, query: $query, pagination: $pagination" }

        val activeWalletsFromConnectedEvents = EventTables.WalletConnectedTable.fetchWallets(query)
        val activeWalletsFromTxEvents = EventTables.TxRequestTable.fetchWallets(query)

        val perPeriodValues = (activeWalletsFromConnectedEvents + activeWalletsFromTxEvents)
            .sortedBy { it.component2().value }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = true,
                pagination = pagination
            )
        val averageValue = if (perPeriodValues.isNotEmpty()) {
            perPeriodValues.sumOf { it.value }.toDouble() / perPeriodValues.size.toDouble()
        } else 0.0

        return IntTimespanWithAverage(
            values = perPeriodValues,
            averageValue = averageValue
        )
    }

    // TODO improve efficiency
    override fun totalTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find total transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { emptyList() }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun totalSuccessfulTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find successful transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { listOf(it.TX.subfield(TxData.TX_DATA.STATUS).eq(TxStatus.SUCCESS)) }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun totalPendingTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find pending transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { listOf(it.TX.subfield(TxData.TX_DATA.STATUS).eq(TxStatus.PENDING)) }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun totalCancelledTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find cancelled transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { listOf(it.TX.subfield(TxData.TX_DATA.STATUS).eq(TxStatus.CANCELLED)) }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun totalFailedTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues> {
        logger.debug { "Find failed transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { listOf(it.TX.subfield(TxData.TX_DATA.STATUS).eq(TxStatus.FAILURE)) }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun averageTransactionsPerUser(
        query: StatisticsQuery,
        pagination: Pagination
    ): Array<AverageTimespanValues> {
        logger.debug { "Find average transactions per user, query: $query, pagination: $pagination" }

        val table = EventTables.TxRequestTable
        val txHashField = table.db.TX.subfield(TxData.TX_DATA.HASH)
        val userIdField = table.tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)

        return dslContext.select(txHashField, userIdField, table.createdAt)
            .from(table.db)
            .where(query.condition(table))
            .orderBy(table.createdAt)
            .fetch {
                Pair(
                    Pair<TransactionHash?, String?>(
                        it.component1()?.let(TransactionHash::invoke),
                        it.component2()
                    ),
                    it.component3()
                )
            }
            .groupByUserAverageByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun averageTransactions(query: StatisticsQuery, pagination: Pagination): MovingAverageTimespanValues {
        logger.debug { "Find average transactions, query: $query, pagination: $pagination" }

        return fetchTransactions(query) { emptyList() }
            .movingAverage(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = pagination
            )
    }

    // TODO improve efficiency
    override fun minTransactionsInPeriod(query: StatisticsQuery): Int {
        logger.debug { "Min transactions in period, query: $query" }

        return fetchTransactions(query) { emptyList() }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = null
            )
            .minOfOrNull { it.value } ?: 0
    }

    // TODO improve efficiency
    override fun maxTransactionsInPeriod(query: StatisticsQuery): Int {
        logger.debug { "Max transactions in period, query: $query" }

        return fetchTransactions(query) { emptyList() }
            .groupByDuration(
                from = query.from,
                to = query.to,
                granularity = query.granularity,
                uniqueInRange = false,
                pagination = null
            )
            .maxOfOrNull { it.value } ?: 0
    }

    // TODO improve efficiency
    override fun listWalletProviders(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug {
            "List wallet providers, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination"
        }
        return listStats(projectId, { it.walletProvider }, eventFilter, pagination)
    }

    // TODO improve efficiency
    override fun listCountries(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List countries, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }
        return listStats(projectId, { it.country }, eventFilter, pagination)
    }

    // TODO improve efficiency
    override fun listNetworks(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfoForNetwork> {
        logger.debug { "List networks, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }

        val walletConnectedKey = EventTables.WalletConnectedTable.chainId
        val walletCounts = dslContext.select(
            walletConnectedKey,
            DSL.count(EventTables.WalletConnectedTable.walletAddress),
            DSL.countDistinct(EventTables.WalletConnectedTable.walletAddress)
        )
            .from(EventTables.WalletConnectedTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.WalletConnectedTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.WalletConnectedTable, projectId),
                        walletConnectedKey.isNotNull
                    )
                )
            )
            .groupBy(walletConnectedKey)
            .orderBy(walletConnectedKey)
            .fetchMap({ it.component1().value }) {
                WalletConnectionsAndTransactionsInfoForNetwork(
                    chainId = it.component1().value,
                    totalWalletConnections = it.component2(),
                    uniqueWalletConnections = it.component3(),
                    executedTransactions = 0,
                    uniqueUsersLanded = 0
                )
            }

        val txRequestKey = EventTables.TxRequestTable.chainId
        val transactionCounts = dslContext.select(txRequestKey, DSL.count(EventTables.TxRequestTable.createdAt))
            .from(EventTables.TxRequestTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.TxRequestTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.TxRequestTable, projectId),
                        txRequestKey.isNotNull
                    )
                )
            )
            .groupBy(txRequestKey)
            .orderBy(txRequestKey)
            .fetchMap({ it.component1().value }) {
                WalletConnectionsAndTransactionsInfoForNetwork(
                    chainId = it.component1().value,
                    totalWalletConnections = 0,
                    uniqueWalletConnections = 0,
                    executedTransactions = it.component2(),
                    uniqueUsersLanded = 0
                )
            }

        val userLandedKey = EventTables.UserLandedTable.chainId
        val userLandedCounts = dslContext.select(
            userLandedKey,
            DSL.countDistinct(EventTables.UserLandedTable.userId)
        )
            .from(EventTables.UserLandedTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.UserLandedTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.UserLandedTable, projectId),
                        userLandedKey.isNotNull
                    )
                )
            )
            .groupBy(userLandedKey)
            .orderBy(userLandedKey)
            .fetchMap({ it.component1()?.value }) {
                WalletConnectionsAndTransactionsInfoForNetwork(
                    chainId = it.component1().value,
                    totalWalletConnections = 0,
                    uniqueWalletConnections = 0,
                    executedTransactions = 0,
                    uniqueUsersLanded = it.component2()
                )
            }

        return (walletCounts.keys + transactionCounts.keys + userLandedCounts.keys).sorted()
            .map {
                val walletCount = walletCounts[it]
                val transactionCount = transactionCounts[it]
                val userLandedCount = userLandedCounts[it]

                WalletConnectionsAndTransactionsInfoForNetwork(
                    chainId = it,
                    totalWalletConnections = walletCount?.totalWalletConnections ?: 0,
                    uniqueWalletConnections = walletCount?.uniqueWalletConnections ?: 0,
                    executedTransactions = transactionCount?.executedTransactions ?: 0,
                    uniqueUsersLanded = userLandedCount?.uniqueUsersLanded ?: 0
                )
            }
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    // TODO improve efficiency
    override fun listBrowsers(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List browsers, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }
        return listStats(projectId, { it.browser }, eventFilter, pagination)
    }

    // TODO improve efficiency
    override fun listReferrers(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List referrers, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }
        return listStats(projectId, { it.referrer }, eventFilter, pagination)
    }

    // TODO improve efficiency
    @Suppress("LongMethod", "ComplexMethod")
    override fun listSessions(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<SessionEventsInfo> {
        logger.debug { "List sessions, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }

        data class SessionEventInfoCollector(
            val sessionId: String,
            val totalEventCount: Int,
            val totalErrorEventCount: Int,
            val walletAddresses: Set<WalletAddress>,
            val hasConnectedWallet: Boolean,
            val hasExecutedTransaction: Boolean,
            val devices: Set<DeviceStateModel>,
            val firstEventDateTime: UtcDateTime
        )

        data class Select(
            val count: Int,
            val wallets: Set<WalletAddress>,
            val devices: Set<DeviceStateModel>,
            val firstEventDateTime: UtcDateTime
        )

        val EMPTY_SELECT = Select(
            count = 0,
            wallets = emptySet(),
            devices = emptySet(),
            firstEventDateTime = UtcDateTime(OffsetDateTime.parse("9999-12-31T23:59:59Z"))
        )

        fun fetchEventCount(table: EventTable<*, *>) =
            dslContext.select(
                table.sessionId,
                DSL.count(),
                PostgresDSL.arrayAggDistinct(table.walletAddress).filterWhere(table.walletAddress.isNotNull),
                PostgresDSL.arrayAggDistinct(table.device).filterWhere(table.device.isNotNull),
                DSL.min(table.createdAt)
            )
                .from(table.db)
                .where(
                    DSL.and(
                        listOfNotNull(
                            table.projectId.eq(projectId),
                            eventFilter?.createCondition(table, projectId)
                        )
                    )
                )
                .groupBy(table.sessionId)
                .fetchMap({ it.component1() }) {
                    Select(
                        count = it.component2() ?: 0,
                        wallets = it.component3()?.toSet() ?: emptySet(),
                        devices = it.component4()?.map { r -> r.toModel() }?.toSet() ?: emptySet(),
                        firstEventDateTime = it.component5() ?: EMPTY_SELECT.firstEventDateTime
                    )
                }

        val walletEventCounts = fetchEventCount(EventTables.WalletConnectedTable)
        val txRequestEventCounts = fetchEventCount(EventTables.TxRequestTable)
        val blockchainErrorEventCounts = fetchEventCount(EventTables.BlockchainErrorTable)
        val errorEventCounts = fetchEventCount(EventTables.ErrorTable)
        val userLandedEventCounts = fetchEventCount(EventTables.UserLandedTable)

        val errorKeys = errorEventCounts.keys + blockchainErrorEventCounts.keys
        val errorCounts = errorKeys.associateWith { key ->
            val blockchainErrors = blockchainErrorEventCounts[key] ?: EMPTY_SELECT
            val errors = errorEventCounts[key] ?: EMPTY_SELECT

            val count = blockchainErrors.count + errors.count
            val wallets = blockchainErrors.wallets + errors.wallets
            val devices = blockchainErrors.devices + errors.devices

            SessionEventInfoCollector(
                sessionId = key,
                totalErrorEventCount = count,
                totalEventCount = count,
                walletAddresses = wallets,
                hasConnectedWallet = false,
                hasExecutedTransaction = false,
                devices = devices,
                firstEventDateTime = blockchainErrors.firstEventDateTime.min(errors.firstEventDateTime)
            )
        }

        val otherKeys = walletEventCounts.keys + txRequestEventCounts.keys + userLandedEventCounts.keys
        val otherCounts = otherKeys.associateWith { key ->
            val walletEvents = walletEventCounts[key] ?: EMPTY_SELECT
            val txRequests = txRequestEventCounts[key] ?: EMPTY_SELECT
            val userLanded = userLandedEventCounts[key] ?: EMPTY_SELECT

            val count = walletEvents.count + txRequests.count + userLanded.count
            val wallets = walletEvents.wallets + txRequests.wallets + userLanded.wallets
            val devices = walletEvents.devices + txRequests.devices + userLanded.devices

            SessionEventInfoCollector(
                sessionId = key,
                totalErrorEventCount = 0,
                totalEventCount = count,
                walletAddresses = wallets,
                hasConnectedWallet = walletEvents.count > 0,
                hasExecutedTransaction = txRequests.count > 0,
                devices = devices,
                firstEventDateTime = walletEvents.firstEventDateTime
                    .min(txRequests.firstEventDateTime)
                    .min(userLanded.firstEventDateTime)
            )
        }

        val allKeys = errorKeys + otherKeys
        val allCounts = allKeys.map { key ->
            val errors = errorCounts[key]
            val other = otherCounts[key]

            val errorWallets = errors?.walletAddresses ?: emptySet()
            val otherWallets = other?.walletAddresses ?: emptySet()

            val errorDevices = errors?.devices ?: emptySet()
            val otherDevices = other?.devices ?: emptySet()

            val errorFirstEventDateTime = errors?.firstEventDateTime ?: EMPTY_SELECT.firstEventDateTime
            val otherFirstEventDateTime = other?.firstEventDateTime ?: EMPTY_SELECT.firstEventDateTime

            SessionEventsInfo(
                sessionId = key,
                totalEventCount = (errors?.totalEventCount ?: 0) + (other?.totalEventCount ?: 0),
                totalErrorEventCount = errors?.totalErrorEventCount ?: 0,
                walletAddresses = (errorWallets + otherWallets).map { it.rawValue }.toTypedArray(),
                hasConnectedWallet = other?.hasConnectedWallet ?: false,
                hasExecutedTransaction = other?.hasExecutedTransaction ?: false,
                devices = (errorDevices + otherDevices).toTypedArray(),
                firstEventDateTime = errorFirstEventDateTime.min(otherFirstEventDateTime).value
            )
        }

        return allCounts
            .sortedByDescending { it.firstEventDateTime }
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    // TODO improve efficiency
    // TODO shares a lot of logic with listSessions
    @Suppress("LongMethod", "ComplexMethod")
    override fun listUsers(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UserEventsInfo> {
        logger.debug { "List users, projectId: $projectId, eventFilter: $eventFilter, pagination: $pagination" }

        data class UserEventInfoCollector(
            val userId: String,
            val sessionIds: Set<String>,
            val totalEventCount: Int,
            val totalErrorEventCount: Int,
            val walletAddresses: Set<WalletAddress>,
            val hasConnectedWallet: Boolean,
            val hasExecutedTransaction: Boolean,
            val devices: Set<DeviceStateModel>,
            val firstEventDateTime: UtcDateTime
        )

        data class Select(
            val count: Int,
            val sessions: Set<String>,
            val wallets: Set<WalletAddress>,
            val devices: Set<DeviceStateModel>,
            val firstEventDateTime: UtcDateTime
        )

        val EMPTY_SELECT = Select(
            count = 0,
            sessions = emptySet(),
            wallets = emptySet(),
            devices = emptySet(),
            firstEventDateTime = UtcDateTime(OffsetDateTime.parse("9999-12-31T23:59:59Z"))
        )

        fun fetchEventCount(table: EventTable<*, *>) =
            dslContext.select(
                table.userId,
                DSL.count(),
                PostgresDSL.arrayAggDistinct(table.sessionId).filterWhere(table.sessionId.isNotNull),
                PostgresDSL.arrayAggDistinct(table.walletAddress).filterWhere(table.walletAddress.isNotNull),
                PostgresDSL.arrayAggDistinct(table.device).filterWhere(table.device.isNotNull),
                DSL.min(table.createdAt)
            )
                .from(table.db)
                .where(
                    DSL.and(
                        listOfNotNull(
                            table.projectId.eq(projectId),
                            eventFilter?.createCondition(table, projectId)
                        )
                    )
                )
                .groupBy(table.userId)
                .fetchMap({ it.component1() }) {
                    Select(
                        count = it.component2() ?: 0,
                        sessions = it.component3()?.toSet() ?: emptySet(),
                        wallets = it.component4()?.toSet() ?: emptySet(),
                        devices = it.component5()?.map { r -> r.toModel() }?.toSet() ?: emptySet(),
                        firstEventDateTime = it.component6() ?: EMPTY_SELECT.firstEventDateTime
                    )
                }

        val walletEventCounts = fetchEventCount(EventTables.WalletConnectedTable)
        val txRequestEventCounts = fetchEventCount(EventTables.TxRequestTable)
        val blockchainErrorEventCounts = fetchEventCount(EventTables.BlockchainErrorTable)
        val errorEventCounts = fetchEventCount(EventTables.ErrorTable)
        val userLandedEventCounts = fetchEventCount(EventTables.UserLandedTable)

        val errorKeys = errorEventCounts.keys + blockchainErrorEventCounts.keys
        val errorCounts = errorKeys.associateWith { key ->
            val blockchainErrors = blockchainErrorEventCounts[key] ?: EMPTY_SELECT
            val errors = errorEventCounts[key] ?: EMPTY_SELECT

            val count = blockchainErrors.count + errors.count
            val sessions = blockchainErrors.sessions + errors.sessions
            val wallets = blockchainErrors.wallets + errors.wallets
            val devices = blockchainErrors.devices + errors.devices

            UserEventInfoCollector(
                userId = key,
                sessionIds = sessions,
                totalErrorEventCount = count,
                totalEventCount = count,
                walletAddresses = wallets,
                hasConnectedWallet = false,
                hasExecutedTransaction = false,
                devices = devices,
                firstEventDateTime = blockchainErrors.firstEventDateTime.min(errors.firstEventDateTime)
            )
        }

        val otherKeys = walletEventCounts.keys + txRequestEventCounts.keys + userLandedEventCounts.keys
        val otherCounts = otherKeys.associateWith { key ->
            val walletEvents = walletEventCounts[key] ?: EMPTY_SELECT
            val txRequests = txRequestEventCounts[key] ?: EMPTY_SELECT
            val userLanded = userLandedEventCounts[key] ?: EMPTY_SELECT

            val count = walletEvents.count + txRequests.count + userLanded.count
            val sessions = walletEvents.sessions + txRequests.sessions + userLanded.sessions
            val wallets = walletEvents.wallets + txRequests.wallets + userLanded.wallets
            val devices = walletEvents.devices + txRequests.devices + userLanded.devices

            UserEventInfoCollector(
                userId = key,
                sessionIds = sessions,
                totalErrorEventCount = 0,
                totalEventCount = count,
                walletAddresses = wallets,
                hasConnectedWallet = walletEvents.count > 0,
                hasExecutedTransaction = txRequests.count > 0,
                devices = devices,
                firstEventDateTime = walletEvents.firstEventDateTime
                    .min(txRequests.firstEventDateTime)
                    .min(userLanded.firstEventDateTime)
            )
        }

        val allKeys = errorKeys + otherKeys
        val allCounts = allKeys.map { key ->
            val errors = errorCounts[key]
            val other = otherCounts[key]

            val errorSessions = errors?.sessionIds ?: emptySet()
            val otherSessions = other?.sessionIds ?: emptySet()

            val errorWallets = errors?.walletAddresses ?: emptySet()
            val otherWallets = other?.walletAddresses ?: emptySet()

            val errorDevices = errors?.devices ?: emptySet()
            val otherDevices = other?.devices ?: emptySet()

            val errorFirstEventDateTime = errors?.firstEventDateTime ?: EMPTY_SELECT.firstEventDateTime
            val otherFirstEventDateTime = other?.firstEventDateTime ?: EMPTY_SELECT.firstEventDateTime

            UserEventsInfo(
                userId = key,
                sessionIds = (errorSessions + otherSessions).toTypedArray(),
                totalEventCount = (errors?.totalEventCount ?: 0) + (other?.totalEventCount ?: 0),
                totalErrorEventCount = errors?.totalErrorEventCount ?: 0,
                walletAddresses = (errorWallets + otherWallets).map { it.rawValue }.toTypedArray(),
                hasConnectedWallet = other?.hasConnectedWallet ?: false,
                hasExecutedTransaction = other?.hasExecutedTransaction ?: false,
                devices = (errorDevices + otherDevices).toTypedArray(),
                firstEventDateTime = errorFirstEventDateTime.min(otherFirstEventDateTime).value
            )
        }

        return allCounts
            .sortedByDescending { it.firstEventDateTime }
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    // TODO improve efficiency
    override fun projectUserStats(
        from: UtcDateTime?,
        to: UtcDateTime?,
        projectId: ProjectId,
        eventFilter: EventFilter?
    ): ProjectUserStats {
        logger.debug {
            "Get project user stats, from: $from, to: $to, projectId: $projectId, eventFilter: $eventFilter"
        }

        data class WalletCount(val n: Int, val withProvider: Int)
        data class TxCount(val n: Int)
        data class WalletInfo(val hasWallet: Boolean, val hasConnected: Boolean)

        fun fetchUserCount(table: EventTable<*, *>) =
            dslContext.select(
                table.userId,
                DSL.countDistinct(table.walletAddress).filterWhere(table.walletAddress.isNotNull),
                DSL.countDistinct(table.walletProvider)
                    .filterWhere(
                        DSL.and(
                            table.walletProvider.isNotNull,
                            table.walletProvider.ne("none")
                        )
                    )
            )
                .from(table.db)
                .where(
                    DSL.and(
                        listOfNotNull(
                            table.projectId.eq(projectId),
                            from?.let { table.createdAt.ge(it) },
                            to?.let { table.createdAt.le(it) },
                            eventFilter?.createCondition(table, projectId)
                        )
                    )
                )
                .groupBy(table.userId)
                .fetchMap({ it.component1() }) {
                    WalletCount(
                        n = it.component2() ?: 0,
                        withProvider = it.component3() ?: 0
                    )
                }

        val walletConnectedCount = fetchUserCount(EventTables.WalletConnectedTable)
        val blockchainErrorCount = fetchUserCount(EventTables.BlockchainErrorTable)
        val errorCount = fetchUserCount(EventTables.ErrorTable)
        val userLandedCount = fetchUserCount(EventTables.UserLandedTable)
        val txRequestUserCount = fetchUserCount(EventTables.TxRequestTable)

        val txRequestCount = dslContext.select(
            EventTables.TxRequestTable.userId,
            DSL.countDistinct(EventTables.TxRequestTable.txHash)
                .filterWhere(
                    DSL.and(
                        EventTables.TxRequestTable.txHash.isNotNull,
                        EventTables.TxRequestTable.txStatus.ne(TxStatus.PENDING)
                    )
                )
        )
            .from(EventTables.TxRequestTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.TxRequestTable.projectId.eq(projectId),
                        from?.let { EventTables.TxRequestTable.createdAt.ge(it) },
                        to?.let { EventTables.TxRequestTable.createdAt.le(it) },
                        eventFilter?.createCondition(EventTables.TxRequestTable, projectId)
                    )
                )
            )
            .groupBy(EventTables.TxRequestTable.userId)
            .fetchMap({ it.component1() }) {
                TxCount(it.component2() ?: 0)
            }

        val keys = walletConnectedCount.keys + blockchainErrorCount.keys + errorCount.keys +
            userLandedCount.keys + txRequestUserCount.keys + txRequestCount.keys
        val walletInfos = keys.map { key ->
            val withProvider = (walletConnectedCount[key]?.withProvider ?: 0) +
                (blockchainErrorCount[key]?.withProvider ?: 0) +
                (errorCount[key]?.withProvider ?: 0) +
                (userLandedCount[key]?.withProvider ?: 0) +
                (txRequestUserCount[key]?.withProvider ?: 0)
            val n = (walletConnectedCount[key]?.n ?: 0) +
                (blockchainErrorCount[key]?.n ?: 0) +
                (errorCount[key]?.n ?: 0) +
                (userLandedCount[key]?.n ?: 0) +
                (txRequestUserCount[key]?.n ?: 0)

            WalletInfo(
                hasWallet = withProvider > 0,
                hasConnected = n > 0
            )
        }

        return ProjectUserStats(
            totalUsers = keys.size,
            usersWithWallet = walletInfos.count { it.hasWallet },
            usersWithConnectedWallet = walletInfos.count { it.hasConnected },
            usersWithExecutedTx = txRequestCount.count { it.value.n > 0 },
            usersWithMultipleExecutedTx = txRequestCount.count { it.value.n > 1 }
        )
    }

    // TODO improve efficiency
    override fun getUserWalletAndTransactionStats(
        field: EventTrackerModelField,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UsersWalletsAndTransactionsInfo> {
        logger.debug {
            "Get project user, wallet and transaction stats, field: $field, projectId: $projectId," +
                " from: $from, to: $to, eventFilter: $eventFilter, pagination: $pagination"
        }

        data class WalletCount(
            val totalWallets: Int,
            val users: Set<String>,
            val usersWithWallets: Set<String>,
            val uniqueWallets: Int
        )

        data class TxCount(
            val totalTx: Int,
            val users: Set<String>,
            val usersWithExecutedTx: Int
        )

        val EMPTY_WALLET_COUNT = WalletCount(0, emptySet(), emptySet(), 0)
        val EMPTY_TX_COUNT = TxCount(0, emptySet(), 0)

        fun fetchUserAndWalletCount(table: EventTable<*, *>): Map<String, WalletCount> {
            val aggregate = field.get(table)

            return dslContext.select(
                aggregate,
                DSL.count(table.walletAddress).filterWhere(table.walletAddress.isNotNull),
                PostgresDSL.arrayAggDistinct(table.userId).filterWhere(table.userId.isNotNull),
                PostgresDSL.arrayAggDistinct(table.userId).filterWhere(
                    DSL.and(
                        table.userId.isNotNull,
                        table.walletAddress.isNotNull
                    )
                ),
                DSL.countDistinct(table.walletAddress).filterWhere(table.walletAddress.isNotNull)
            )
                .from(table.db)
                .where(
                    DSL.and(
                        listOfNotNull(
                            aggregate.isNotNull,
                            table.projectId.eq(projectId),
                            from?.let { table.createdAt.ge(it) },
                            to?.let { table.createdAt.le(it) },
                            eventFilter?.createCondition(table, projectId),
                        )
                    )
                )
                .groupBy(aggregate)
                .fetchMap({ it.component1() }) {
                    WalletCount(
                        totalWallets = it.component2() ?: 0,
                        users = it.component3()?.toSet() ?: emptySet(),
                        usersWithWallets = it.component4()?.toSet() ?: emptySet(),
                        uniqueWallets = it.component5() ?: 0
                    )
                }
        }

        val walletConnectedCount = fetchUserAndWalletCount(EventTables.WalletConnectedTable)
        val blockchainErrorCount = fetchUserAndWalletCount(EventTables.BlockchainErrorTable)
        val errorCount = fetchUserAndWalletCount(EventTables.ErrorTable)
        val userLandedCount = fetchUserAndWalletCount(EventTables.UserLandedTable)
        val txReqAggregate = field.get(EventTables.TxRequestTable)
        val txRequestCount = dslContext.select(
            txReqAggregate,
            DSL.countDistinct(EventTables.TxRequestTable.txHash)
                .filterWhere(
                    DSL.and(
                        EventTables.TxRequestTable.txHash.isNotNull,
                        EventTables.TxRequestTable.txStatus.ne(TxStatus.PENDING)
                    )
                ),
            PostgresDSL.arrayAggDistinct(EventTables.TxRequestTable.userId).filterWhere(
                EventTables.TxRequestTable.userId.isNotNull
            ),
            DSL.countDistinct(EventTables.TxRequestTable.userId).filterWhere(
                DSL.and(
                    EventTables.TxRequestTable.txHash.isNotNull,
                    EventTables.TxRequestTable.txStatus.ne(TxStatus.PENDING)
                )
            )
        )
            .from(EventTables.TxRequestTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        txReqAggregate.isNotNull,
                        EventTables.TxRequestTable.projectId.eq(projectId),
                        EventTables.TxRequestTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.TxRequestTable, projectId)
                    )
                )
            )
            .groupBy(txReqAggregate)
            .fetchMap({ it.component1() }) {
                TxCount(
                    totalTx = it.component2() ?: 0,
                    users = it.component3()?.toSet() ?: emptySet(),
                    usersWithExecutedTx = it.component4() ?: 0
                )
            }

        val keys = walletConnectedCount.keys + blockchainErrorCount.keys + errorCount.keys +
            userLandedCount.keys + txRequestCount.keys

        val result = keys.map { key ->
            val wcc = walletConnectedCount[key] ?: EMPTY_WALLET_COUNT
            val bec = blockchainErrorCount[key] ?: EMPTY_WALLET_COUNT
            val ec = errorCount[key] ?: EMPTY_WALLET_COUNT
            val ulc = userLandedCount[key] ?: EMPTY_WALLET_COUNT
            val txc = txRequestCount[key] ?: EMPTY_TX_COUNT

            UsersWalletsAndTransactionsInfo(
                name = key,
                totalUsers = (wcc.users + bec.users + ec.users + ulc.users + txc.users).size,
                usersWithWallet = (wcc.usersWithWallets + ulc.usersWithWallets).size,
                usersWithConnectedWallet = wcc.usersWithWallets.size,
                totalWalletConnections = wcc.totalWallets,
                uniqueWalletConnections = wcc.uniqueWallets,
                executedTransactions = txc.totalTx,
                usersWithExecutedTx = txc.usersWithExecutedTx
            )
        }

        return result
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    private fun listStats(
        projectId: ProjectId,
        key: (EventTable<*, *>) -> Field<out String?>,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        val walletConnectedKey = key(EventTables.WalletConnectedTable)
        val walletCounts = dslContext.select(
            walletConnectedKey,
            DSL.count(EventTables.WalletConnectedTable.walletAddress),
            DSL.countDistinct(EventTables.WalletConnectedTable.walletAddress)
        )
            .from(EventTables.WalletConnectedTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.WalletConnectedTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.WalletConnectedTable, projectId)
                    )
                )
            )
            .groupBy(walletConnectedKey)
            .orderBy(walletConnectedKey)
            .fetchMap({ it.component1() ?: "unknown" }) {
                WalletConnectionsAndTransactionsInfo(
                    name = it.component1() ?: "unknown",
                    totalWalletConnections = it.component2(),
                    uniqueWalletConnections = it.component3(),
                    executedTransactions = 0,
                    uniqueUsersLanded = 0
                )
            }

        val txRequestKey = key(EventTables.TxRequestTable)
        val transactionCounts = dslContext.select(txRequestKey, DSL.count(EventTables.TxRequestTable.createdAt))
            .from(EventTables.TxRequestTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.TxRequestTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.TxRequestTable, projectId)
                    )
                )
            )
            .groupBy(txRequestKey)
            .orderBy(txRequestKey)
            .fetchMap({ it.component1() ?: "unknown" }) {
                WalletConnectionsAndTransactionsInfo(
                    name = it.component1() ?: "unknown",
                    totalWalletConnections = 0,
                    uniqueWalletConnections = 0,
                    executedTransactions = it.component2(),
                    uniqueUsersLanded = 0
                )
            }

        val userLandedKey = key(EventTables.UserLandedTable)
        val userLandedCounts = dslContext.select(
            userLandedKey,
            DSL.countDistinct(EventTables.UserLandedTable.userId)
        )
            .from(EventTables.UserLandedTable.db)
            .where(
                DSL.and(
                    listOfNotNull(
                        EventTables.UserLandedTable.projectId.eq(projectId),
                        eventFilter?.createCondition(EventTables.UserLandedTable, projectId)
                    )
                )
            )
            .groupBy(userLandedKey)
            .orderBy(userLandedKey)
            .fetchMap({ it.component1() ?: "unknown" }) {
                WalletConnectionsAndTransactionsInfo(
                    name = it.component1() ?: "unknown",
                    totalWalletConnections = 0,
                    uniqueWalletConnections = 0,
                    executedTransactions = 0,
                    uniqueUsersLanded = it.component2()
                )
            }

        return (walletCounts.keys + transactionCounts.keys + userLandedCounts.keys).sorted()
            .map {
                val walletCount = walletCounts[it]
                val transactionCount = transactionCounts[it]
                val userLandedCount = userLandedCounts[it]

                WalletConnectionsAndTransactionsInfo(
                    name = it,
                    totalWalletConnections = walletCount?.totalWalletConnections ?: 0,
                    uniqueWalletConnections = walletCount?.uniqueWalletConnections ?: 0,
                    executedTransactions = transactionCount?.executedTransactions ?: 0,
                    uniqueUsersLanded = userLandedCount?.uniqueUsersLanded ?: 0
                )
            }
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    private fun fetchTransactions(
        query: StatisticsQuery,
        extraConditions: (TxRequestEventTable) -> List<Condition>
    ): List<Pair<TransactionHash?, UtcDateTime>> {
        val table = EventTables.TxRequestTable
        val txHashField = table.db.TX.subfield(TxData.TX_DATA.HASH)
        val conditions = DSL.and(
            listOf(query.condition(table)) + extraConditions(table.db)
        )

        return dslContext.select(txHashField, table.createdAt)
            .from(table.db)
            .where(conditions)
            .orderBy(table.createdAt)
            .fetch { Pair(it.component1()?.let(TransactionHash::invoke), it.component2()) }
    }

    private fun fetchUniqueWalletConnectedEvents(query: StatisticsQuery): List<Pair<WalletAddress, UtcDateTime>> {
        val table = EventTables.WalletConnectedTable

        val walletAddressField = table.walletAddress
        val minCreatedAtField = DSL.min(table.createdAt)

        return dslContext.select(walletAddressField, minCreatedAtField)
            .from(table.db)
            .where(query.condition(table))
            .groupBy(table.walletAddress)
            .orderBy(minCreatedAtField)
            .fetch { Pair(it.component1(), it.component2()) }
    }

    private fun <R : Record, T : TableImpl<R>> EventTable<R, T>.fetchWallets(
        query: StatisticsQuery
    ): List<Pair<WalletAddress, UtcDateTime>> =
        dslContext.select(this.walletAddress, this.createdAt)
            .from(this.db)
            .where(query.condition(this))
            .orderBy(this.createdAt)
            .fetch { Pair(it.component1(), it.component2()) }

    private fun List<Pair<Pair<TransactionHash?, String?>, UtcDateTime>>.groupByUserAverageByDuration(
        from: UtcDateTime?,
        to: UtcDateTime?,
        granularity: Duration?,
        pagination: Pagination
    ): Array<AverageTimespanValues> {
        val start = from ?: firstOrNull()?.component2() ?: return emptyArray()
        val end = to ?: lastOrNull()?.component2()

        val grouping = when (granularity) {
            null -> mapOf(TimeRange(start, last().component2(), 0) to this)
            is ExactDuration -> groupBy { it.groupByExactDuration(start, granularity) }
                .expandExactDuration(start, end, granularity)

            is InexactDuration -> groupBy { it.groupByInexactDuration(granularity) } // TODO expand
        }

        return grouping.map {
            val txPerUserInRange = it.value.groupBy { e -> e.first.second }.map { e -> e.value.size }
            val averagePerUserInRange = txPerUserInRange.average().takeIf { n -> n.isNaN().not() } ?: 0.0

            AverageTimespanValues(
                from = it.key.start.value,
                to = it.key.end.value,
                averageValue = averagePerUserInRange
            )
        }
            .drop(pagination.offset)
            .take(pagination.limit)
            .toTypedArray()
    }

    private fun <T> List<Pair<T, UtcDateTime>>.groupByDuration(
        from: UtcDateTime?,
        to: UtcDateTime?,
        granularity: Duration?,
        uniqueInRange: Boolean,
        pagination: Pagination?
    ): Array<IntTimespanValues> {
        logger.debug { "Group by duration, number of items: ${this.size}" }
        val start = from ?: firstOrNull()?.component2() ?: return emptyArray()
        val end = to ?: lastOrNull()?.component2()

        val grouping = when (granularity) {
            null -> mapOf(TimeRange(start, last().component2(), 0) to this)
            is ExactDuration -> groupBy { it.groupByExactDuration(start, granularity) }
                .expandExactDuration(start, end, granularity)

            is InexactDuration -> groupBy { it.groupByInexactDuration(granularity) } // TODO expand
        }

        val result = grouping.map {
            val size = if (uniqueInRange) {
                it.value.distinctBy { v -> v.component1() }.size
            } else it.value.size

            IntTimespanValues(
                from = it.key.start.value,
                to = it.key.end.value,
                value = size
            )
        }

        return if (pagination != null) {
            result
                .drop(pagination.offset)
                .take(pagination.limit)
                .toTypedArray()
        } else {
            result.toTypedArray()
        }
    }

    private fun <T> Pair<T, UtcDateTime>.groupByExactDuration(
        start: UtcDateTime,
        limit: ExactDuration
    ): TimeRange {
        val diff = this.component2() - start
        val scale = floor(diff / limit).toInt()
        return TimeRange(
            start = start + limit * scale,
            end = start + limit * (scale + 1) - ExactDuration(1.milliseconds),
            scale = scale
        )
    }

    private fun <T> Map<TimeRange, List<Pair<T, UtcDateTime>>>.expandExactDuration(
        start: UtcDateTime,
        end: UtcDateTime?,
        limit: ExactDuration
    ): Map<TimeRange, List<Pair<T, UtcDateTime>>> {
        val maxDiff = (end ?: UtcDateTime(OffsetDateTime.now())) - start
        val maxScale = floor(maxDiff / limit).toInt()

        val fullRange = (0..maxScale).associate { scale ->
            val timeRange = TimeRange(
                start = start + limit * scale,
                end = start + limit * (scale + 1) - ExactDuration(1.milliseconds),
                scale = scale
            )

            val item = this[timeRange] ?: emptyList()

            Pair(timeRange, item)
        }

        return fullRange
    }

    private fun <T> Pair<T, UtcDateTime>.groupByInexactDuration(
        duration: InexactDuration
    ): TimeRange {
        val time = this.component2()

        return when (duration) {
            MonthlyDuration -> TimeRange(
                time.atMonthStart(time.month()),
                time.atMonthEnd(time.month()),
                0
            ) // TODO scale
            YearlyDuration -> TimeRange(time.atYearStart(), time.atYearEnd(), 0) // TODO scale
        }
    }

    private fun <T> List<Pair<T, UtcDateTime>>.movingAverage(
        from: UtcDateTime?,
        to: UtcDateTime?,
        granularity: Duration?,
        uniqueInRange: Boolean,
        pagination: Pagination
    ): MovingAverageTimespanValues {
        val grouped = this.groupByDuration(
            from = from,
            to = to,
            granularity = granularity,
            uniqueInRange = uniqueInRange,
            pagination = null
        )

        val movingAverages = grouped.fold(mutableListOf<WithCount<IntTimespanValues>>()) { acc, elem ->
            val totalCount = acc.lastOrNull()?.count ?: elem.value.toLong()
            val withCount = WithCount(elem, totalCount)
            acc.add(withCount)
            acc
        }.map {
            AverageTimespanValues(
                from = it.value.from,
                to = it.value.to,
                averageValue = it.value.value.toDouble() / it.count.toDouble()
            )
        }

        val averageValue = if (movingAverages.isNotEmpty()) {
            grouped.sumOf { it.value }.toDouble() / movingAverages.size
        } else 0.0

        return MovingAverageTimespanValues(
            movingAverages = movingAverages.drop(pagination.offset).take(pagination.limit),
            averageValue = averageValue
        )
    }

    private fun <R : Record> StatisticsQuery.condition(eventTable: EventTable<R, *>): Condition =
        DSL.and(
            listOfNotNull(
                eventTable.projectId.eq(this.projectId),
                this.from?.let { eventTable.createdAt.ge(it) },
                this.to?.let { eventTable.createdAt.le(it) },
                this.eventFilter?.createCondition(eventTable, projectId)
            )
        )
}

private fun DeviceStateRecord.toModel() =
    DeviceState(
        os = os,
        browser = browser,
        country = country,
        screen = screen?.let {
            ScreenState(
                w = it.w!!,
                h = it.h!!
            )
        },
        walletProvider = walletProvider!!,
        walletType = walletType!!
    )
