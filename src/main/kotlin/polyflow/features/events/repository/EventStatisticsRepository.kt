package polyflow.features.events.repository

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
import polyflow.util.UtcDateTime

@Suppress("TooManyFunctions")
interface EventStatisticsRepository {
    fun totalConnectedWallets(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues>
    fun totalNewWallets(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues>
    fun periodActiveWallets(query: StatisticsQuery, pagination: Pagination): IntTimespanWithAverage
    fun totalTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues>
    fun totalSuccessfulTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues>
    fun totalCancelledTransactions(query: StatisticsQuery, pagination: Pagination): Array<IntTimespanValues>
    fun averageTransactionsPerUser(query: StatisticsQuery, pagination: Pagination): Array<AverageTimespanValues>
    fun averageTransactions(query: StatisticsQuery, pagination: Pagination): MovingAverageTimespanValues
    fun minTransactionsInPeriod(query: StatisticsQuery): Int
    fun maxTransactionsInPeriod(query: StatisticsQuery): Int
    fun listWalletProviders(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listCountries(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listNetworks(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfoForNetwork>

    fun listBrowsers(
        projectId: ProjectId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listSessions(projectId: ProjectId, eventFilter: EventFilter?, pagination: Pagination): Array<SessionEventsInfo>
    fun listUsers(projectId: ProjectId, eventFilter: EventFilter?, pagination: Pagination): Array<UserEventsInfo>
    fun projectUserStats(projectId: ProjectId, eventFilter: EventFilter?): ProjectUserStats
    fun getUserWalletAndTransactionStats(
        field: EventTrackerModelField,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UsersWalletsAndTransactionsInfo>
}
