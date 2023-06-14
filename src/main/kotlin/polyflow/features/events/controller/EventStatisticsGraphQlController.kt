package polyflow.features.events.controller

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
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
import polyflow.features.events.service.EventStatisticsService
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Duration
import polyflow.util.UtcDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Validated
@Controller
@Suppress("TooManyFunctions")
class EventStatisticsGraphQlController(
    private val eventStatisticsService: EventStatisticsService,
    private val userRepository: UserRepository
) { // TODO test

    companion object {
        private val MIN_DATETIME = OffsetDateTime.parse("2020-01-01T00:00:00Z")
    }

    @QueryMapping
    fun totalConnectedWallets(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalConnectedWallets(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun totalNewWallets(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalNewWallets(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun periodActiveWallets(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): IntTimespanWithAverage {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.periodActiveWallets(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        )
    }

    @QueryMapping
    fun totalTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun totalSuccessfulTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalSuccessfulTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun totalPendingTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalPendingTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun totalCancelledTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalCancelledTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun totalFailedTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<IntTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.totalFailedTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleIntElement(from, to)
    }

    @QueryMapping
    fun averageTransactionsPerUser(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<AverageTimespanValues> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.averageTransactionsPerUser(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        ).orSingleDoubleElement(from, to)
    }

    @QueryMapping
    fun averageTransactions(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): MovingAverageTimespanValues {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.averageTransactions(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id,
            pagination = pagination
        )
    }

    @QueryMapping
    fun minTransactionsInPeriod(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?
    ): Int {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.minTransactionsInPeriod(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id
        )
    }

    @QueryMapping
    fun maxTransactionsInPeriod(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument granularity: Duration?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?
    ): Int {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.maxTransactionsInPeriod(
            StatisticsQuery(
                from = from?.let(UtcDateTime::invoke),
                to = to?.let(UtcDateTime::invoke),
                granularity = granularity,
                projectId = ProjectId(projectId),
                eventFilter = filter
            ),
            userId = user.id
        )
    }

    @QueryMapping
    fun listWalletProviders(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listWalletProviders(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun listCountries(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listCountries(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun listNetworks(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfoForNetwork> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listNetworks(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun listBrowsers(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<WalletConnectionsAndTransactionsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listBrowsers(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun listSessions(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<SessionEventsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listSessions(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun listUsers(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<UserEventsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.listUsers(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter,
            pagination = pagination
        )
    }

    @QueryMapping
    fun projectUserStats(
        @Argument projectId: UUID,
        @Argument filter: EventFilter?
    ): ProjectUserStats {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.projectUserStats(
            projectId = ProjectId(projectId),
            userId = user.id,
            eventFilter = filter
        )
    }

    @QueryMapping
    fun getUserWalletAndTransactionStats(
        @Argument field: EventTrackerModelField,
        @Argument projectId: UUID,
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument filter: EventFilter?,
        @Argument pagination: Pagination
    ): Array<UsersWalletsAndTransactionsInfo> {
        val user = Util.resolveUser(userRepository)
        return eventStatisticsService.getUserWalletAndTransactionStats(
            field = field,
            projectId = ProjectId(projectId),
            userId = user.id,
            from = from?.let(UtcDateTime::invoke),
            to = to?.let(UtcDateTime::invoke),
            eventFilter = filter,
            pagination = pagination
        )
    }

    private fun Array<IntTimespanValues>.orSingleIntElement(from: OffsetDateTime?, to: OffsetDateTime?) =
        ifEmpty { arrayOf(IntTimespanValues(from ?: MIN_DATETIME, to ?: now(), 0)) }

    private fun Array<AverageTimespanValues>.orSingleDoubleElement(from: OffsetDateTime?, to: OffsetDateTime?) =
        ifEmpty { arrayOf(AverageTimespanValues(from ?: MIN_DATETIME, to ?: now(), 0.0)) }

    private fun now() = UtcDateTime(OffsetDateTime.now()).value // TODO use provider instead!
}
