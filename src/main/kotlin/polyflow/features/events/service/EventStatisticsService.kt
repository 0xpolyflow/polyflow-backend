package polyflow.features.events.service

import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.params.StatisticsQuery
import polyflow.features.events.model.response.AverageTimespanValues
import polyflow.features.events.model.response.IntTimespanValues
import polyflow.features.events.model.response.IntTimespanWithAverage
import polyflow.features.events.model.response.MovingAverageTimespanValues
import polyflow.features.events.model.response.WalletConnectionsAndTransactionsInfo
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId

@Suppress("TooManyFunctions")
interface EventStatisticsService {
    fun totalConnectedWallets(
        query: StatisticsQuery,
        userId: UserId
    ): List<IntTimespanValues>

    fun totalNewWallets(
        query: StatisticsQuery,
        userId: UserId
    ): List<IntTimespanValues>

    fun periodActiveWallets(
        query: StatisticsQuery,
        userId: UserId
    ): IntTimespanWithAverage

    fun totalTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): List<IntTimespanValues>

    fun totalSuccessfulTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): List<IntTimespanValues>

    fun totalCancelledTransactions(
        query: StatisticsQuery,
        userId: UserId
    ): List<IntTimespanValues>

    fun averageTransactionsPerUser(
        query: StatisticsQuery,
        userId: UserId
    ): List<AverageTimespanValues>

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
    ): List<WalletConnectionsAndTransactionsInfo>

    fun listCountries(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): List<WalletConnectionsAndTransactionsInfo>

    fun listBrowsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): List<WalletConnectionsAndTransactionsInfo>
}
