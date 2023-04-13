package polyflow.features.events.repository

import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.params.StatisticsQuery
import polyflow.features.events.model.response.AverageTimespanValues
import polyflow.features.events.model.response.IntTimespanValues
import polyflow.features.events.model.response.IntTimespanWithAverage
import polyflow.features.events.model.response.MovingAverageTimespanValues
import polyflow.features.events.model.response.SessionEventsInfo
import polyflow.features.events.model.response.WalletConnectionsAndTransactionsInfo
import polyflow.generated.jooq.id.ProjectId

@Suppress("TooManyFunctions")
interface EventStatisticsRepository {
    fun totalConnectedWallets(query: StatisticsQuery): Array<IntTimespanValues>
    fun totalNewWallets(query: StatisticsQuery): Array<IntTimespanValues>
    fun periodActiveWallets(query: StatisticsQuery): IntTimespanWithAverage
    fun totalTransactions(query: StatisticsQuery): Array<IntTimespanValues>
    fun totalSuccessfulTransactions(query: StatisticsQuery): Array<IntTimespanValues>
    fun totalCancelledTransactions(query: StatisticsQuery): Array<IntTimespanValues>
    fun averageTransactionsPerUser(query: StatisticsQuery): Array<AverageTimespanValues>
    fun averageTransactions(query: StatisticsQuery): MovingAverageTimespanValues
    fun minTransactionsInPeriod(query: StatisticsQuery): Int
    fun maxTransactionsInPeriod(query: StatisticsQuery): Int
    fun listWalletProviders(
        projectId: ProjectId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo>

    fun listCountries(projectId: ProjectId, eventFilter: EventFilter?): Array<WalletConnectionsAndTransactionsInfo>
    fun listBrowsers(projectId: ProjectId, eventFilter: EventFilter?): Array<WalletConnectionsAndTransactionsInfo>
    fun listSessions(projectId: ProjectId, eventFilter: EventFilter?): Array<SessionEventsInfo>
}
