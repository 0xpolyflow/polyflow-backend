package polyflow.features.events.service

import mu.KLogging
import org.springframework.stereotype.Service
import polyflow.exception.AccessForbiddenException
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
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch total connected wallets, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalConnectedWallets(query, pagination)
    }

    override fun totalNewWallets(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch new connected wallets, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalNewWallets(query, pagination)
    }

    override fun periodActiveWallets(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): IntTimespanWithAverage {
        logger.debug {
            "Request to fetch period active wallets, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.periodActiveWallets(query, pagination)
    }

    override fun totalTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug { "Request to fetch total transactions, query: $query, userId: $userId, pagination: $pagination" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalTransactions(query, pagination)
    }

    override fun totalSuccessfulTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch total successful transactions, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalSuccessfulTransactions(query, pagination)
    }

    override fun totalPendingTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch total pending transactions, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalPendingTransactions(query, pagination)
    }

    override fun totalCancelledTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch total cancelled transactions, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalCancelledTransactions(query, pagination)
    }

    override fun totalFailedTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<IntTimespanValues> {
        logger.debug {
            "Request to fetch total failed transactions, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.totalFailedTransactions(query, pagination)
    }

    override fun averageTransactionsPerUser(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): Array<AverageTimespanValues> {
        logger.debug {
            "Request to average transactions per user, query: $query, userId: $userId, pagination: $pagination"
        }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.averageTransactionsPerUser(query, pagination)
    }

    override fun averageTransactions(
        query: StatisticsQuery,
        userId: UserId,
        pagination: Pagination
    ): MovingAverageTimespanValues {
        logger.debug { "Request to average transactions, query: $query, userId: $userId, pagination: $pagination" }

        requireProjectReadAccess(userId, query.projectId)

        return eventStatisticsRepository.averageTransactions(query, pagination)
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
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug {
            "List wallet providers, projectId: $projectId, userId: $userId, eventFilter: $eventFilter," +
                " pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listWalletProviders(projectId, eventFilter, pagination)
    }

    override fun listCountries(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug {
            "List countries, projectId: $projectId, userId: $userId, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listCountries(projectId, eventFilter, pagination)
    }

    override fun listNetworks(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfoForNetwork> {
        logger.debug {
            "List networks, projectId: $projectId, userId: $userId, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listNetworks(projectId, eventFilter, pagination)
    }

    override fun listBrowsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        logger.debug {
            "List browsers, projectId: $projectId, userId: $userId, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listBrowsers(projectId, eventFilter, pagination)
    }

    override fun listSessions(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<SessionEventsInfo> {
        logger.debug {
            "List sessions, projectId: $projectId, userId: $userId, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listSessions(projectId, eventFilter, pagination)
    }

    override fun listUsers(
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UserEventsInfo> {
        logger.debug {
            "List users, projectId: $projectId, userId: $userId, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.listUsers(projectId, eventFilter, pagination)
    }

    override fun projectUserStats(
        from: UtcDateTime?,
        to: UtcDateTime?,
        projectId: ProjectId,
        userId: UserId,
        eventFilter: EventFilter?
    ): ProjectUserStats {
        logger.debug {
            "Get project user stats, from: $from, to: $to, projectId: $projectId," +
                " userId: $userId, eventFilter: $eventFilter"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.projectUserStats(
            from = from,
            to = to,
            projectId = projectId,
            eventFilter = eventFilter
        )
    }

    override fun getUserWalletAndTransactionStats(
        field: EventTrackerModelField,
        projectId: ProjectId,
        userId: UserId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): Array<UsersWalletsAndTransactionsInfo> {
        logger.debug {
            "Get project user, wallet and transaction stats, field: $field, projectId: $projectId, userId: $userId," +
                " from: $from, to: $to, eventFilter: $eventFilter, pagination: $pagination"
        }

        requireProjectReadAccess(userId, projectId)

        return eventStatisticsRepository.getUserWalletAndTransactionStats(
            field = field,
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter,
            pagination = pagination
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
