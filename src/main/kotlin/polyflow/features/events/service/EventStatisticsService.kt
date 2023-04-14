package polyflow.features.events.service

import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.params.StatisticsQuery
import polyflow.features.events.model.request.filter.EventTrackerModelField
import polyflow.features.events.model.response.AverageTimespanValues
import polyflow.features.events.model.response.IntTimespanValues
import polyflow.features.events.model.response.IntTimespanWithAverage
import polyflow.features.events.model.response.MovingAverageTimespanValues
import polyflow.features.events.model.response.ProjectUserStats
import polyflow.features.events.model.response.SessionEventsInfo
import polyflow.features.events.model.response.UsersWalletsAndTransactionsInfo
import polyflow.features.events.model.response.WalletConnectionsAndTransactionsInfo
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

@Suppress("TooManyFunctions")
interface EventStatisticsService {
    fun totalConnectedWallets(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues>

    fun totalNewWallets(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues>

    fun periodActiveWallets(
        query: StatisticsQuery,
        userId: UserId
    ): IntTimespanWithAverage

    fun totalTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues>

    fun totalSuccessfulTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues>

    fun totalCancelledTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues>

    fun averageTransactionsPerUser(
        query: StatisticsQuery,
        userId: UserId
    ): Array<AverageTimespanValues>

    fun averageTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): MovingAverageTimespanValues

    fun minTransactionsInPeriod(
        query: StatisticsQuery,
        userId: UserId
    ): Int

    fun maxTransactionsInPeriod(
        query: StatisticsQuery,
        userId: UserId
    ): Int

    fun listWalletProviders(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listCountries(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listBrowsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listSessions(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<SessionEventsInfo>

    fun projectUserStats(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): ProjectUserStats

    fun getUserWalletAndTransactionStats(
        field: EventTrackerModelField,
        projectId: ProjectId,
        userId: UserId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): Array<UsersWalletsAndTransactionsInfo>
}
