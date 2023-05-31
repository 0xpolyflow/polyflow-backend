package polyflow.features.events.service

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
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

@Suppress("TooManyFunctions")
interface EventStatisticsService {
    fun totalConnectedWallets(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues>

    fun totalNewWallets(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues>

    fun periodActiveWallets(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): IntTimespanWithAverage

    fun totalTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues>

    fun totalSuccessfulTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues>

    fun totalCancelledTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues>

    fun averageTransactionsPerUser(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<AverageTimespanValues>

    fun averageTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
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
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listCountries(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listNetworks(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfoForNetwork>

    fun listBrowsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listSessions(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<SessionEventsInfo>

    fun listUsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UserEventsInfo>

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
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UsersWalletsAndTransactionsInfo>
}
