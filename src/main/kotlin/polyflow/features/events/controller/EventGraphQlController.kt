package polyflow.features.events.controller

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.ContextValue
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.request.BlockchainErrorEventRequest
import polyflow.features.events.model.request.ErrorEventRequest
import polyflow.features.events.model.request.TxRequestEventRequest
import polyflow.features.events.model.request.UserLandedEventRequest
import polyflow.features.events.model.request.WalletConnectedEventRequest
import polyflow.features.events.model.request.filter.DeviceStateField
import polyflow.features.events.model.request.filter.EventTrackerModelField
import polyflow.features.events.model.response.BlockchainErrorEvent
import polyflow.features.events.model.response.ErrorEvent
import polyflow.features.events.model.response.EventResponse
import polyflow.features.events.model.response.TxRequestEvent
import polyflow.features.events.model.response.UniqueValues
import polyflow.features.events.model.response.UserLandedEvent
import polyflow.features.events.model.response.WalletConnectedEvent
import polyflow.features.events.service.EventService
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.EventId
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.UtcDateTime
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID
import javax.validation.Valid

@Validated
@Controller
class EventGraphQlController(
    private val eventService: EventService,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository
) { // TODO test

    @QueryMapping
    fun findEventById(@Argument id: UUID): EventResponse {
        val user = Util.resolveUser(userRepository)
        return eventService.findEventById(eventId = EventId(id), userId = user.id)
    }

    @QueryMapping
    fun findEvents(
        @Argument from: OffsetDateTime?,
        @Argument to: OffsetDateTime?,
        @Argument projectId: UUID,
        @Argument filter: EventFilter?
    ): List<EventResponse> {
        val user = Util.resolveUser(userRepository)
        return eventService.findEvents(
            userId = user.id,
            projectId = ProjectId(projectId),
            from = from?.let(UtcDateTime::invoke),
            to = to?.let(UtcDateTime::invoke),
            eventFilter = filter
        )
    }

    @QueryMapping
    fun findUniqueValues(
        eventTrackerFields: Array<EventTrackerModelField>,
        deviceStateFields: Array<DeviceStateField>,
        projectId: UUID,
        from: OffsetDateTime?,
        to: OffsetDateTime?,
        filter: EventFilter?
    ): UniqueValues {
        val user = Util.resolveUser(userRepository)
        return eventService.findUniqueValues(
            fields = eventTrackerFields.toSet() + deviceStateFields.toSet(),
            userId = user.id,
            projectId = ProjectId(projectId),
            from = from?.let(UtcDateTime::invoke),
            to = to?.let(UtcDateTime::invoke),
            eventFilter = filter
        )
    }

    @MutationMapping
    fun createWalletConnectedEvent(
        @Valid @Argument event: WalletConnectedEventRequest,
        @ContextValue apiKey: Optional<String>
    ): WalletConnectedEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.create(project.id, event)
    }

    @MutationMapping
    fun createTxRequestEvent(
        @Valid @Argument event: TxRequestEventRequest,
        @ContextValue apiKey: Optional<String>
    ): TxRequestEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.create(project.id, event)
    }

    @MutationMapping
    fun createErrorEvent(
        @Valid @Argument event: ErrorEventRequest,
        @ContextValue apiKey: Optional<String>
    ): ErrorEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.create(project.id, event)
    }

    @MutationMapping
    fun createBlockchainErrorEvent(
        @Valid @Argument event: BlockchainErrorEventRequest,
        @ContextValue apiKey: Optional<String>
    ): BlockchainErrorEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.create(project.id, event)
    }

    @MutationMapping
    fun createUserLandedEvent(
        @Valid @Argument event: UserLandedEventRequest,
        @ContextValue apiKey: Optional<String>
    ): UserLandedEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.create(project.id, event)
    }

    @MutationMapping
    fun updateTxRequestEventTxStatus(
        @Argument id: UUID,
        @Argument newStatus: TxStatus,
        @ContextValue apiKey: Optional<String>
    ): TxRequestEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.updateTxRequestEventTxStatus(project.id, EventId(id), newStatus)
    }

    @MutationMapping
    fun updateBlockchainErrorEventTxStatus(
        @Argument id: UUID,
        @Argument newStatus: TxStatus,
        @ContextValue apiKey: Optional<String>
    ): BlockchainErrorEvent {
        val project = Util.resolveProject(projectRepository, userRepository, apiKey)
        return eventService.updateBlockchainErrorEventTxStatus(project.id, EventId(id), newStatus)
    }
}
