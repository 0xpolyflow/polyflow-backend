@file:Suppress("TooManyFunctions")

package polyflow.features.events.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.SelectField
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl
import org.springframework.stereotype.Repository
import polyflow.features.events.model.DeviceState
import polyflow.features.events.model.EventTrackerModel
import polyflow.features.events.model.NetworkState
import polyflow.features.events.model.ScreenState
import polyflow.features.events.model.TxData
import polyflow.features.events.model.WalletState
import polyflow.features.events.model.params.EventFilter
import polyflow.features.events.model.request.BlockchainErrorEventRequest
import polyflow.features.events.model.request.ErrorEventRequest
import polyflow.features.events.model.request.TxRequestEventRequest
import polyflow.features.events.model.request.UserLandedEventRequest
import polyflow.features.events.model.request.WalletConnectedEventRequest
import polyflow.features.events.model.request.filter.DeviceStateField
import polyflow.features.events.model.request.filter.EventTrackerModelField
import polyflow.features.events.model.request.filter.FieldGetter
import polyflow.features.events.model.response.BlockchainErrorEvent
import polyflow.features.events.model.response.DeviceStateUniqueValues
import polyflow.features.events.model.response.ErrorEvent
import polyflow.features.events.model.response.EventResponse
import polyflow.features.events.model.response.EventTrackerModelUniqueValues
import polyflow.features.events.model.response.TxRequestEvent
import polyflow.features.events.model.response.UniqueValues
import polyflow.features.events.model.response.UserLandedEvent
import polyflow.features.events.model.response.WalletConnectedEvent
import polyflow.generated.jooq.enums.TxStatus
import polyflow.generated.jooq.id.EventId
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.tables.BlockchainErrorEventTable
import polyflow.generated.jooq.tables.TxRequestEventTable
import polyflow.generated.jooq.tables.records.BlockchainErrorEventRecord
import polyflow.generated.jooq.tables.records.ErrorEventRecord
import polyflow.generated.jooq.tables.records.TxRequestEventRecord
import polyflow.generated.jooq.tables.records.UserLandedEventRecord
import polyflow.generated.jooq.tables.records.WalletConnectedEventRecord
import polyflow.generated.jooq.udt.records.DeviceStateRecord
import polyflow.generated.jooq.udt.records.EventTrackerModelRecord
import polyflow.generated.jooq.udt.records.NetworkStateRecord
import polyflow.generated.jooq.udt.records.ScreenStateRecord
import polyflow.generated.jooq.udt.records.TxDataRecord
import polyflow.generated.jooq.udt.records.WalletStateRecord
import polyflow.util.ChainId
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress
import polyflow.generated.jooq.udt.TxData as TD

@Repository
class JooqEventRepository(private val dslContext: DSLContext) : EventRepository { // TODO test

    companion object : KLogging()

    override fun findEventById(eventId: EventId): EventResponse? {
        logger.info { "Request event by id: $eventId" }

        fun <R : Record, T : TableImpl<R>> EventTable<R, T>.byId(): R? =
            dslContext.selectFrom(this.db)
                .where(this.id.eq(eventId.value))
                .fetchOne()

        return EventTables.WalletConnectedTable.byId()?.toModel()
            ?: EventTables.TxRequestTable.byId()?.toModel()
            ?: EventTables.ErrorTable.byId()?.toModel()
            ?: EventTables.BlockchainErrorTable.byId()?.toModel()
            ?: EventTables.UserLandedTable.byId()?.toModel()
    }

    override fun findEvents(
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): List<EventResponse> {
        logger.info { "Find events for projectId: $projectId, from: $from, to: $to, utmFilter: $eventFilter" }

        val walletConnectedEvents = EventTables.WalletConnectedTable.findEventsQuery(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        ).fetch { it.toModel() }

        val txRequestEvents = EventTables.TxRequestTable.findEventsQuery(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        ).fetch { it.toModel() }

        val errorEvents = EventTables.ErrorTable.findEventsQuery(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        ).fetch { it.toModel() }

        val blockchainErrorEvents = EventTables.BlockchainErrorTable.findEventsQuery(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        ).fetch { it.toModel() }

        val userLandedEvents = EventTables.UserLandedTable.findEventsQuery(
            projectId = projectId,
            from = from,
            to = to,
            eventFilter = eventFilter
        ).fetch { it.toModel() }

        return (walletConnectedEvents + txRequestEvents + errorEvents + blockchainErrorEvents + userLandedEvents)
            .sortedBy { it.createdAt.value }
    }

    override fun findUniqueValues(
        fields: Set<FieldGetter>,
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): UniqueValues {

        fun <T> fetchDistinct(table: EventTable<*, *>, field: FieldGetter): SelectConditionStep<Record1<T>> {
            val conditions = listOfNotNull(
                field.get(table).isNotNull,
                table.projectId.eq(projectId),
                from?.let { table.createdAt.le(it) },
                to?.let { table.createdAt.ge(it) },
                eventFilter?.createCondition(table)
            )

            @Suppress("UNCHECKED_CAST") // we need type info to combine multiple unions in fetchDistinctFromAllTables
            return dslContext.selectDistinct(field.get(table) as SelectField<T>)
                .where(DSL.and(conditions))
        }

        @Suppress("UNCHECKED_CAST") // casting from Set<*> to Set<T> - since we have only one field we use fetchSet(0)
        fun <T> fetchDistinctFromAllTables(field: FieldGetter): Set<T> =
            fetchDistinct<T>(EventTables.WalletConnectedTable, field)
                .union(fetchDistinct(EventTables.TxRequestTable, field))
                .union(fetchDistinct(EventTables.BlockchainErrorTable, field))
                .union(fetchDistinct(EventTables.ErrorTable, field))
                .union(fetchDistinct(EventTables.UserLandedTable, field))
                .fetchSet(0) as Set<T>

        val fetchedValues = fields.associateWith { fetchDistinctFromAllTables<Any>(it) }

        return UniqueValues(
            tracker = EventTrackerModelUniqueValues(
                eventTracker = fetchedValues.forField(EventTrackerModelField.EVENT_TRACKER),
                sessionId = fetchedValues.forField(EventTrackerModelField.SESSION_ID),
                utmSource = fetchedValues.forField(EventTrackerModelField.UTM_SOURCE),
                utmMedium = fetchedValues.forField(EventTrackerModelField.UTM_MEDIUM),
                utmCampaign = fetchedValues.forField(EventTrackerModelField.UTM_CAMPAIGN),
                utmContent = fetchedValues.forField(EventTrackerModelField.UTM_CONTENT),
                utmTerm = fetchedValues.forField(EventTrackerModelField.UTM_TERM),
                origin = fetchedValues.forField(EventTrackerModelField.ORIGIN),
                path = fetchedValues.forField(EventTrackerModelField.PATH)
            ),
            device = DeviceStateUniqueValues(
                os = fetchedValues.forField(DeviceStateField.OS),
                browser = fetchedValues.forField(DeviceStateField.BROWSER),
                country = fetchedValues.forField(DeviceStateField.COUNTRY),
                screen = fetchedValues.forField<ScreenStateRecord>(DeviceStateField.SCREEN)
                    ?.map { it.toModel() }?.toTypedArray(),
                walletProvider = fetchedValues.forField(DeviceStateField.WALLET_PROVIDER),
                walletType = fetchedValues.forField(DeviceStateField.WALLET_TYPE)
            )
        )
    }

    override fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: WalletConnectedEventRequest
    ): WalletConnectedEvent {
        logger.info { "Create event, id: $id, projectId: $projectId, createdAt: $createdAt, event: $event" }

        val record = WalletConnectedEventRecord(
            id = id.value,
            projectId = projectId,
            createdAt = createdAt,
            tracker = event.tracker.toRecord(),
            wallet = event.wallet.toRecord(),
            device = event.device.toRecord(),
            network = event.network.toRecord()
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: TxRequestEventRequest
    ): TxRequestEvent {
        logger.info { "Create event, id: $id, projectId: $projectId, createdAt: $createdAt, event: $event" }

        val record = TxRequestEventRecord(
            id = id.value,
            projectId = projectId,
            createdAt = createdAt,
            tracker = event.tracker.toRecord(),
            wallet = event.wallet.toRecord(),
            device = event.device.toRecord(),
            network = event.network.toRecord(),
            tx = event.tx.toRecord()
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: BlockchainErrorEventRequest
    ): BlockchainErrorEvent {
        logger.info { "Create event, id: $id, projectId: $projectId, createdAt: $createdAt, event: $event" }

        val record = BlockchainErrorEventRecord(
            id = id.value,
            projectId = projectId,
            createdAt = createdAt,
            tracker = event.tracker.toRecord(),
            errors = event.errors.toTypedArray(),
            wallet = event.wallet.toRecord(),
            device = event.device.toRecord(),
            network = event.network.toRecord(),
            tx = event.tx.toRecord()
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: ErrorEventRequest
    ): ErrorEvent {
        logger.info { "Create event, id: $id, projectId: $projectId, createdAt: $createdAt, event: $event" }

        val record = ErrorEventRecord(
            id = id.value,
            projectId = projectId,
            createdAt = createdAt,
            tracker = event.tracker.toRecord(),
            errors = event.errors.toTypedArray(),
            wallet = event.wallet?.toRecord(),
            device = event.device.toRecord(),
            network = event.network?.toRecord()
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun create(
        id: EventId,
        projectId: ProjectId,
        createdAt: UtcDateTime,
        event: UserLandedEventRequest
    ): UserLandedEvent {
        logger.info { "Create event, id: $id, projectId: $projectId, createdAt: $createdAt, event: $event" }

        val record = UserLandedEventRecord(
            id = id.value,
            projectId = projectId,
            createdAt = createdAt,
            tracker = event.tracker.toRecord(),
            wallet = event.wallet?.toRecord(),
            device = event.device.toRecord(),
            network = event.network?.toRecord()
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun updateTxRequestEventTxStatus(id: EventId, projectId: ProjectId, newStatus: TxStatus): TxRequestEvent? {
        logger.info { "Update tx event tx status, id: $id, projectId: $projectId, newStatus: $newStatus" }

        return dslContext.update(TxRequestEventTable)
            .set(TxRequestEventTable.TX.setSubfield(TD.TX_DATA.STATUS), newStatus)
            .where(
                DSL.and(
                    TxRequestEventTable.ID.eq(id.value),
                    TxRequestEventTable.PROJECT_ID.eq(projectId)
                )
            )
            .returning()
            .fetchOne { it.toModel() }
    }

    override fun updateBlockchainErrorEventTxStatus(
        id: EventId,
        projectId: ProjectId,
        newStatus: TxStatus
    ): BlockchainErrorEvent? {
        logger.info { "Update blockchain error event tx status, id: $id, projectId: $projectId, newStatus: $newStatus" }

        return dslContext.update(BlockchainErrorEventTable)
            .set(BlockchainErrorEventTable.TX.setSubfield(TD.TX_DATA.STATUS), newStatus)
            .where(
                DSL.and(
                    BlockchainErrorEventTable.ID.eq(id.value),
                    BlockchainErrorEventTable.PROJECT_ID.eq(projectId)
                )
            )
            .returning()
            .fetchOne { it.toModel() }
    }

    private fun <R : Record, T : TableImpl<R>> EventTable<R, T>.findEventsQuery(
        projectId: ProjectId,
        from: UtcDateTime?,
        to: UtcDateTime?,
        eventFilter: EventFilter?
    ): SelectConditionStep<R> {
        val conditions = listOfNotNull(
            this.projectId.eq(projectId),
            from?.let { this.createdAt.le(it) },
            to?.let { this.createdAt.ge(it) },
            eventFilter?.createCondition(this)
        )

        return dslContext.selectFrom(this.db)
            .where(DSL.and(conditions))
    }
}

private fun EventTrackerModel.toRecord() =
    EventTrackerModelRecord(
        eventTracker = eventTracker,
        userId = userId,
        sessionId = sessionId,
        utmSource = utmSource,
        utmMedium = utmMedium,
        utmCampaign = utmCampaign,
        utmContent = utmContent,
        utmTerm = utmTerm,
        origin = origin,
        path = path
    )

private fun EventTrackerModelRecord.toModel() =
    EventTrackerModel(
        eventTracker = eventTracker!!,
        userId = userId!!,
        sessionId = sessionId!!,
        utmSource = utmSource,
        utmMedium = utmMedium,
        utmCampaign = utmCampaign,
        utmContent = utmContent,
        utmTerm = utmTerm,
        origin = origin,
        path = path
    )

private fun WalletState.toRecord() =
    WalletStateRecord(
        walletAddress = WalletAddress(walletAddress),
        gasBalance = gasBalance,
        nonce = nonce,
        networkId = networkId
    )

private fun WalletStateRecord.toModel() =
    WalletState(
        walletAddress = walletAddress?.rawValue!!,
        gasBalance = gasBalance!!,
        nonce = nonce!!,
        networkId = networkId!!
    )

private fun DeviceState.toRecord() =
    DeviceStateRecord(
        os = os,
        browser = browser,
        country = country,
        screen = screen?.let {
            ScreenStateRecord(
                w = it.w,
                h = it.h
            )
        },
        walletProvider = walletProvider,
        walletType = walletType
    )

private fun ScreenStateRecord.toModel() =
    ScreenState(
        w = w!!,
        h = h!!
    )

private fun DeviceStateRecord.toModel() =
    DeviceState(
        os = os,
        browser = browser,
        country = country,
        screen = screen?.toModel(),
        walletProvider = walletProvider!!,
        walletType = walletType!!
    )

private fun NetworkState.toRecord() =
    NetworkStateRecord(
        chainId = ChainId(chainId),
        gasPrice = gasPrice,
        blockHeight = blockHeight
    )

private fun NetworkStateRecord.toModel() =
    NetworkState(
        chainId = chainId?.value!!,
        gasPrice = gasPrice!!,
        blockHeight = blockHeight!!
    )

private fun TxData.toRecord() =
    TxDataRecord(
        fromAddress = WalletAddress(from),
        toAddress = to?.let(::WalletAddress),
        txValue = value,
        txInput = input,
        nonce = nonce,
        gas = gas,
        gasPrice = gasPrice,
        maxFeePerGas = maxFeePerGas,
        maxPriorityFeePerGas = maxPriorityFeePerGas,
        v = v,
        r = r,
        s = s,
        hash = hash,
        status = status
    )

private fun TxDataRecord.toModel() =
    TxData(
        from = fromAddress?.rawValue!!,
        to = toAddress?.rawValue,
        value = txValue,
        input = txInput,
        nonce = nonce!!,
        gas = gas!!,
        gasPrice = gasPrice!!,
        maxFeePerGas = maxFeePerGas,
        maxPriorityFeePerGas = maxPriorityFeePerGas,
        v = v,
        r = r,
        s = s,
        hash = hash,
        status = status!!
    )

private fun WalletConnectedEventRecord.toModel() =
    WalletConnectedEvent(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        tracker = tracker.toModel(),
        wallet = wallet.toModel(),
        device = device.toModel(),
        network = network.toModel()
    )

private fun TxRequestEventRecord.toModel() =
    TxRequestEvent(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        tracker = tracker.toModel(),
        wallet = wallet.toModel(),
        device = device.toModel(),
        network = network.toModel(),
        tx = tx.toModel()
    )

private fun BlockchainErrorEventRecord.toModel() =
    BlockchainErrorEvent(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        tracker = tracker.toModel(),
        errors = errors.toList(),
        wallet = wallet.toModel(),
        device = device.toModel(),
        network = network.toModel(),
        tx = tx.toModel()
    )

private fun ErrorEventRecord.toModel() =
    ErrorEvent(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        tracker = tracker.toModel(),
        errors = errors.toList(),
        wallet = wallet?.toModel(),
        device = device.toModel(),
        network = network?.toModel()
    )

private fun UserLandedEventRecord.toModel() =
    UserLandedEvent(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        tracker = tracker.toModel(),
        wallet = wallet?.toModel(),
        device = device.toModel(),
        network = network?.toModel()
    )

// Kotlin bullshit: inline functions cannot be nested inside other functions
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Map<FieldGetter, Set<Any>>.forField(field: FieldGetter): Array<T>? =
    (this[field] as? Set<T>)?.toTypedArray()
