package polyflow.features.events.repository

import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.request.BlockchainErrorEventRequest
import polyflow.features.events.model.request.ErrorEventRequest
import polyflow.features.events.model.request.TxRequestEventRequest
import polyflow.features.events.model.request.UserLandedEventRequest
import polyflow.features.events.model.request.WalletConnectedEventRequest
import polyflow.features.events.model.response.BlockchainErrorEvent
import polyflow.features.events.model.response.ErrorEvent
import polyflow.features.events.model.response.EventResponse
import polyflow.features.events.model.response.TxRequestEvent
import polyflow.features.events.model.response.UserLandedEvent
import polyflow.features.events.model.response.WalletConnectedEvent
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.EventId
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.UtcDateTime

interface EventRepository {
    fun findEventById(eventId: EventId): EventResponse?

    fun findEvents(
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): List<EventResponse>

    fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: WalletConnectedEventRequest
    ): WalletConnectedEvent

    fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: TxRequestEventRequest
    ): TxRequestEvent

    fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: BlockchainErrorEventRequest
    ): BlockchainErrorEvent

    fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: ErrorEventRequest
    ): ErrorEvent

    fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: UserLandedEventRequest
    ): UserLandedEvent

    fun updateTxRequestEventTxStatus(id: EventId, projectId: ProjectId, newStatus: TxStatus): TxRequestEvent?
    fun updateBlockchainErrorEventTxStatus(
        id: EventId,
        projectId: ProjectId,
        newStatus: TxStatus
    ): BlockchainErrorEvent?
}
