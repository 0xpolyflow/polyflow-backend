package polyflow.features.events.service

import mu.KLogging
import org.springframework.stereotype.Service
import polyflow.exception.AccessForbiddenException
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
import polyflow.features.events.repository.EventStatisticsRepository
import polyflow.features.project.repository.ProjectRepository
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

@Service
@Suppress("TooManyFunctions")
class EventStatisticsServiceImpl(
    private val projectRepository: ProjectRepository,
    private val eventStatisticsRepository: EventStatisticsRepository
) : EventStatisticsService { // TODO test

    companion object : KLogging()

    override fun totalConnectedWallets(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues> {
        logger.debug { "Request to fetch total connected wallets, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalConnectedWallets(query)
    }

    override fun totalNewWallets(
        query: StatisticsQuery,
        userId: UserId
    ): Array<IntTimespanValues> {
        logger.debug { "Request to fetch new connected wallets, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalNewWallets(query)
    }

    override fun periodActiveWallets(query: StatisticsQuery, userId: UserId): IntTimespanWithAverage {
        logger.debug { "Request to fetch period active wallets, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.periodActiveWallets(query)
    }

    override fun totalTransactions(query: StatisticsQuery, userId: UserId): Array<IntTimespanValues> {
        logger.debug { "Request to fetch total transactions, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalTransactions(query)
    }

    override fun totalSuccessfulTransactions(query: StatisticsQuery, userId: UserId): Array<IntTimespanValues> {
        logger.debug { "Request to fetch total successful transactions, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalSuccessfulTransactions(query)
    }

    override fun totalCancelledTransactions(query: StatisticsQuery, userId: UserId): Array<IntTimespanValues> {
        logger.debug { "Request to fetch total cancelled transactions, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalCancelledTransactions(query)
    }

    override fun averageTransactionsPerUser(query: StatisticsQuery, userId: UserId): Array<AverageTimespanValues> {
        logger.debug { "Request to average transactions per user, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.averageTransactionsPerUser(query)
    }

    override fun averageTransactions(query: StatisticsQuery, userId: UserId): MovingAverageTimespanValues {
        logger.debug { "Request to average transactions, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.averageTransactions(query)
    }

    override fun minTransactionsInPeriod(query: StatisticsQuery, userId: UserId): Int {
        logger.debug { "Request min transactions in period, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.minTransactionsInPeriod(query)
    }

    override fun maxTransactionsInPeriod(query: StatisticsQuery, userId: UserId): Int {
        logger.debug { "Request min transactions in period, query: $query, userId: $userId" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.maxTransactionsInPeriod(query)
    }

    override fun listWalletProviders(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List wallet providers, projectId: $projectId, userId: $userId, eventFilter: $eventFilter" }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listWalletProviders(projectId, eventFilter)
    }

    override fun listCountries(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List countries, projectId: $projectId, userId: $userId, eventFilter: $eventFilter" }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listCountries(projectId, eventFilter)
    }

    override fun listBrowsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug { "List browsers, projectId: $projectId, userId: $userId, eventFilter: $eventFilter" }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listBrowsers(projectId, eventFilter)
    }

    override fun listSessions(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): Array<SessionEventsInfo> {
        logger.debug { "List sessions, projectId: $projectId, userId: $userId, eventFilter: $eventFilter" }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listSessions(projectId, eventFilter)
    }

    override fun projectUserStats(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): ProjectUserStats {
        logger.debug { "Get project user stats, projectId: $projectId, userId: $userId, eventFilter: $eventFilter" }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.projectUserStats(projectId, eventFilter)
    }

    override fun getUserWalletAndTransactionStats(
        field: EventTrackerModelField,
        projectId: ProjectId,
        userId: UserId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): Array<UsersWalletsAndTransactionsInfo> {
        logger.debug {
            "Get project user, wallet and transaction stats, field: $field, projectId: $projectId, userId: $userId," +
                " from: $from, to: $to, eventFilter: $eventFilter"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.getUserWalletAndTransactionStats(
            field = field,
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        )
    }

    private fun requireProjectReadAccess(userId: UserId, projectId: ProjectId) {
        if (projectRepository.hasProjectReadAccess(userId, projectId).not()) {
            throw AccessForbiddenException(
                "Requesting user does not have access to project with id: ${projectId.value}"
            )
        }
    }
}
