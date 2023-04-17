package polyflow.features.events.service

import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.request.BlockchainErrorEventRequest
import polyflow.features.events.model.request.ErrorEventRequest
import polyflow.features.events.model.request.TxRequestEventRequest
import polyflow.features.events.model.request.UserLandedEventRequest
import polyflow.features.events.model.request.WalletConnectedEventRequest
import polyflow.features.events.model.request.filter.FieldGetter
import polyflow.features.events.model.response.BlockchainErrorEvent
import polyflow.features.events.model.response.ErrorEvent
import polyflow.features.events.model.response.EventResponse
import polyflow.features.events.model.response.TxRequestEvent
import polyflow.features.events.model.response.UniqueValues
import polyflow.features.events.model.response.UserLandedEvent
import polyflow.features.events.model.response.WalletConnectedEvent
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.EventId
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

interface EventService {
    fun findEventById(eventId: EventId, userId: UserId): EventResponse
    fun findEvents(
        userId: UserId,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): List<EventResponse>

    fun findUniqueValues(
        fields: Set<FieldGetter>,
        userId: UserId,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): UniqueValues

    fun create(projectId: ProjectId, event: WalletConnectedEventRequest): WalletConnectedEvent
    fun create(projectId: ProjectId, event: TxRequestEventRequest): TxRequestEvent
    fun create(projectId: ProjectId, event: BlockchainErrorEventRequest): BlockchainErrorEvent
    fun create(projectId: ProjectId, event: ErrorEventRequest): ErrorEvent
    fun create(projectId: ProjectId, event: UserLandedEventRequest): UserLandedEvent
    fun updateTxRequestEventTxStatus(projectId: ProjectId, eventId: EventId, newStatus: TxStatus): TxRequestEvent
    fun updateBlockchainErrorEventTxStatus(
        projectId: ProjectId,
        eventId: EventId,
        newStatus: TxStatus
    ): BlockchainErrorEvent
}
