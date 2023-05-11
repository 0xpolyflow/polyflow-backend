package polyflow.features.events.service

import mu.KLogging
import org.springframework.stereotype.Service
import polyflow.exception.ResourceNotFoundException
import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.request.BlockchainErrorEventRequest
import polyflow.features.events.model.request.ErrorEventRequest
import polyflow.features.events.model.request.TxRequestEventRequest
import polyflow.features.events.model.request.UserLandedEventRequest
import polyflow.features.events.model.request.WalletConnectedEventRequest
import polyflow.features.events.model.request.filter.FieldGetter
import polyflow.features.events.model.request.filter.Pagination
import polyflow.features.events.model.response.BlockchainErrorEvent
import polyflow.features.events.model.response.ErrorEvent
import polyflow.features.events.model.response.EventCounts
import polyflow.features.events.model.response.EventResponse
import polyflow.features.events.model.response.TxRequestEvent
import polyflow.features.events.model.response.UniqueValues
import polyflow.features.events.model.response.UserLandedEvent
import polyflow.features.events.model.response.WalletConnectedEvent
import polyflow.features.events.repository.EventRepository
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.project.requireProjectAccess
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.EventId
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime
import polyflow.util.UtcDateTimeProvider
import polyflow.util.UuidProvider

@Service
class EventServiceImpl(
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository,
    private val uuidProvider: UuidProvider,
    private val utcDateTimeProvider: UtcDateTimeProvider
) : EventService { // TODO test

    companion object : KLogging()

    override fun findEventById(eventId: EventId, userId: UserId): EventResponse {
        logger.info { "Request event by id: $eventId, userId: $userId" }

        return eventRepository.findEventById(eventId)?.apply {
            projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)
        } ?: throw ResourceNotFoundException("Event with given ID does not exist for specified project API key")
    }

    override fun findEvents(
        userId: UserId,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): List<EventResponse> {
        logger.info {
            "Request to fetch events, userId: $userId, projectId: $projectId," +
                " from: $from, to: $to, eventFilter: $eventFilter, pagination: $pagination"
        }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return eventRepository.findEvents(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter,
            pagination = pagination
        )
    }

    override fun findUniqueValues(
        fields: Set<FieldGetter>,
        userId: UserId,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): UniqueValues {
        logger.info {
            "Request to fetch unique values, fields: $fields, userId: $userId, projectId: $projectId," +
                " from: $from, to: $to, eventFilter: $eventFilter, pagination: $pagination"
        }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return eventRepository.findUniqueValues(
            fields = fields,
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter,
            pagination = pagination
        )
    }

    override fun findEventCounts(
        fields: Set<FieldGetter>,
        userId: UserId,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?,
        pagination: Pagination
    ): EventCounts {
        logger.info {
            "Request to fetch event counts values, fields: $fields, userId: $userId, projectId: $projectId," +
                " from: $from, to: $to, eventFilter: $eventFilter, pagination: $pagination"
        }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return eventRepository.findEventCounts(
            fields = fields,
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter,
            pagination = pagination
        )
    }

    override fun create(projectId: ProjectId, event: WalletConnectedEventRequest): WalletConnectedEvent {
        logger.info { "Request to create event, projectId: $projectId, event: $event" }

        return eventRepository.create(
            id = uuidProvider.getUuid(EventId),
            projectId = projectId,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            event = event
        )
    }

    override fun create(projectId: ProjectId, event: TxRequestEventRequest): TxRequestEvent {
        logger.info { "Request to create event, projectId: $projectId, event: $event" }

        return eventRepository.create(
            id = uuidProvider.getUuid(EventId),
            projectId = projectId,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            event = event
        )
    }

    override fun create(projectId: ProjectId, event: BlockchainErrorEventRequest): BlockchainErrorEvent {
        logger.info { "Request to create event, projectId: $projectId, event: $event" }

        return eventRepository.create(
            id = uuidProvider.getUuid(EventId),
            projectId = projectId,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            event = event
        )
    }

    override fun create(projectId: ProjectId, event: ErrorEventRequest): ErrorEvent {
        logger.info { "Request to create event, projectId: $projectId, event: $event" }

        return eventRepository.create(
            id = uuidProvider.getUuid(EventId),
            projectId = projectId,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            event = event
        )
    }

    override fun create(projectId: ProjectId, event: UserLandedEventRequest): UserLandedEvent {
        logger.info { "Request to create event, projectId: $projectId, event: $event" }

        return eventRepository.create(
            id = uuidProvider.getUuid(EventId),
            projectId = projectId,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            event = event
        )
    }

    override fun updateTxRequestEventTxStatus(
        projectId: ProjectId,
        eventId: EventId,
        newStatus: TxStatus
    ): TxRequestEvent {
        logger.info { "Update tx event tx status, projectId: $projectId, eventId: $eventId, newStatus: $newStatus" }

        return eventRepository.updateTxRequestEventTxStatus(eventId, projectId, newStatus)
            ?: throw ResourceNotFoundException("Event with given ID does not exist for specified project API key")
    }

    override fun updateBlockchainErrorEventTxStatus(
        projectId: ProjectId,
        eventId: EventId,
        newStatus: TxStatus
    ): BlockchainErrorEvent {
        logger.info {
            "Update blockchain error event tx status, projectId: $projectId, eventId: $eventId, newStatus: $newStatus"
        }

        return eventRepository.updateBlockchainErrorEventTxStatus(eventId, projectId, newStatus)
            ?: throw ResourceNotFoundException("Event with given ID does not exist for specified project API key")
    }
}
