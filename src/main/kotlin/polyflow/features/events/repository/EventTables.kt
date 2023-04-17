package polyflow.features.events.repository

import org.jooq.Record
import org.jooq.TableField
import org.jooq.UDTField
import org.jooq.UDTRecord
import org.jooq.impl.DSL
import polyflow.generated.jooq.tables.BlockchainErrorEventTable
import polyflow.generated.jooq.tables.ErrorEventTable
import polyflow.generated.jooq.tables.TxRequestEventTable
import polyflow.generated.jooq.tables.UserLandedEventTable
import polyflow.generated.jooq.tables.WalletConnectedEventTable
import polyflow.generated.jooq.tables.records.BlockchainErrorEventRecord
import polyflow.generated.jooq.tables.records.ErrorEventRecord
import polyflow.generated.jooq.tables.records.TxRequestEventRecord
import polyflow.generated.jooq.tables.records.UserLandedEventRecord
import polyflow.generated.jooq.tables.records.WalletConnectedEventRecord
import polyflow.generated.jooq.udt.DeviceState
import polyflow.generated.jooq.udt.EventTrackerModel
import polyflow.generated.jooq.udt.TxData
import polyflow.generated.jooq.udt.WalletState

fun <R : Record, U : UDTRecord<U>, V> TableField<R, out U?>.setSubfield(field: UDTField<U, V>) =
    DSL.field("{0}.{1}", field.dataType, this, field.unqualifiedName)

fun <R : Record, U : UDTRecord<U>, V> TableField<R, out U?>.subfield(field: UDTField<U, V>) =
    DSL.field("({0}).{1}", field.dataType, this, field.unqualifiedName)

fun <R : Record, U1 : UDTRecord<U1>, U2 : UDTRecord<U2>, V> TableField<R, U1>.subfield(
    field1: UDTField<U1, U2>,
    field2: UDTField<U2, V>
) = DSL.field("({0}).{1}.{2}", field2.dataType, this, field1.unqualifiedName, field2.unqualifiedName)

object EventTables {
    object WalletConnectedTable : EventTable<WalletConnectedEventRecord, WalletConnectedEventTable> {
        override val db = WalletConnectedEventTable

        override val id = db.ID
        override val projectId = db.PROJECT_ID
        override val createdAt = db.CREATED_AT

        override val tracker = db.TRACKER
        override val wallet = db.WALLET
        override val device = db.DEVICE
        override val network = db.NETWORK

        override val walletAddress = wallet.subfield(WalletState.WALLET_STATE.WALLET_ADDRESS)

        override val os = device.subfield(DeviceState.DEVICE_STATE.OS)
        override val browser = device.subfield(DeviceState.DEVICE_STATE.BROWSER)
        override val country = device.subfield(DeviceState.DEVICE_STATE.COUNTRY)
        override val screen = device.subfield(DeviceState.DEVICE_STATE.SCREEN)
        override val walletProvider = device.subfield(DeviceState.DEVICE_STATE.WALLET_PROVIDER)

        override val eventTracker = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.EVENT_TRACKER)
        override val sessionId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.SESSION_ID)
        override val userId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)
        override val utmSource = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_SOURCE)
        override val utmMedium = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_MEDIUM)
        override val utmCampaign = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CAMPAIGN)
        override val utmContent = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CONTENT)
        override val utmTerm = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_TERM)
        override val origin = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.ORIGIN)
        override val path = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.PATH)
    }

    object TxRequestTable : EventTable<TxRequestEventRecord, TxRequestEventTable> {
        override val db = TxRequestEventTable

        val txHash = db.TX.subfield(TxData.TX_DATA.HASH)
        val txStatus = db.TX.subfield(TxData.TX_DATA.STATUS)

        override val id = db.ID
        override val projectId = db.PROJECT_ID
        override val createdAt = db.CREATED_AT

        override val tracker = db.TRACKER
        override val wallet = db.WALLET
        override val device = db.DEVICE
        override val network = db.NETWORK

        override val walletAddress = wallet.subfield(WalletState.WALLET_STATE.WALLET_ADDRESS)

        override val os = device.subfield(DeviceState.DEVICE_STATE.OS)
        override val browser = device.subfield(DeviceState.DEVICE_STATE.BROWSER)
        override val country = device.subfield(DeviceState.DEVICE_STATE.COUNTRY)
        override val screen = device.subfield(DeviceState.DEVICE_STATE.SCREEN)
        override val walletProvider = device.subfield(DeviceState.DEVICE_STATE.WALLET_PROVIDER)

        override val eventTracker = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.EVENT_TRACKER)
        override val sessionId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.SESSION_ID)
        override val userId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)
        override val utmSource = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_SOURCE)
        override val utmMedium = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_MEDIUM)
        override val utmCampaign = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CAMPAIGN)
        override val utmContent = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CONTENT)
        override val utmTerm = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_TERM)
        override val origin = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.ORIGIN)
        override val path = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.PATH)
    }

    object BlockchainErrorTable : EventTable<BlockchainErrorEventRecord, BlockchainErrorEventTable> {
        override val db = BlockchainErrorEventTable

        override val id = db.ID
        override val projectId = db.PROJECT_ID
        override val createdAt = db.CREATED_AT

        override val tracker = db.TRACKER
        override val wallet = db.WALLET
        override val device = db.DEVICE
        override val network = db.NETWORK

        override val walletAddress = wallet.subfield(WalletState.WALLET_STATE.WALLET_ADDRESS)

        override val os = device.subfield(DeviceState.DEVICE_STATE.OS)
        override val browser = device.subfield(DeviceState.DEVICE_STATE.BROWSER)
        override val country = device.subfield(DeviceState.DEVICE_STATE.COUNTRY)
        override val screen = device.subfield(DeviceState.DEVICE_STATE.SCREEN)
        override val walletProvider = device.subfield(DeviceState.DEVICE_STATE.WALLET_PROVIDER)

        override val eventTracker = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.EVENT_TRACKER)
        override val sessionId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.SESSION_ID)
        override val userId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)
        override val utmSource = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_SOURCE)
        override val utmMedium = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_MEDIUM)
        override val utmCampaign = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CAMPAIGN)
        override val utmContent = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CONTENT)
        override val utmTerm = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_TERM)
        override val origin = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.ORIGIN)
        override val path = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.PATH)
    }

    object ErrorTable : EventTable<ErrorEventRecord, ErrorEventTable> {
        override val db = ErrorEventTable

        override val id = db.ID
        override val projectId = db.PROJECT_ID
        override val createdAt = db.CREATED_AT

        override val tracker = db.TRACKER
        override val wallet = db.WALLET
        override val device = db.DEVICE
        override val network = db.NETWORK

        override val walletAddress = wallet.subfield(WalletState.WALLET_STATE.WALLET_ADDRESS)

        override val os = device.subfield(DeviceState.DEVICE_STATE.OS)
        override val browser = device.subfield(DeviceState.DEVICE_STATE.BROWSER)
        override val country = device.subfield(DeviceState.DEVICE_STATE.COUNTRY)
        override val screen = device.subfield(DeviceState.DEVICE_STATE.SCREEN)
        override val walletProvider = device.subfield(DeviceState.DEVICE_STATE.WALLET_PROVIDER)

        override val eventTracker = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.EVENT_TRACKER)
        override val sessionId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.SESSION_ID)
        override val userId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)
        override val utmSource = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_SOURCE)
        override val utmMedium = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_MEDIUM)
        override val utmCampaign = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CAMPAIGN)
        override val utmContent = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CONTENT)
        override val utmTerm = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_TERM)
        override val origin = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.ORIGIN)
        override val path = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.PATH)
    }

    object UserLandedTable : EventTable<UserLandedEventRecord, UserLandedEventTable> {
        override val db = UserLandedEventTable

        override val id = db.ID
        override val projectId = db.PROJECT_ID
        override val createdAt = db.CREATED_AT

        override val tracker = db.TRACKER
        override val wallet = db.WALLET
        override val device = db.DEVICE
        override val network = db.NETWORK

        override val walletAddress = wallet.subfield(WalletState.WALLET_STATE.WALLET_ADDRESS)

        override val os = device.subfield(DeviceState.DEVICE_STATE.OS)
        override val browser = device.subfield(DeviceState.DEVICE_STATE.BROWSER)
        override val country = device.subfield(DeviceState.DEVICE_STATE.COUNTRY)
        override val screen = device.subfield(DeviceState.DEVICE_STATE.SCREEN)
        override val walletProvider = device.subfield(DeviceState.DEVICE_STATE.WALLET_PROVIDER)

        override val eventTracker = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.EVENT_TRACKER)
        override val sessionId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.SESSION_ID)
        override val userId = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.USER_ID)
        override val utmSource = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_SOURCE)
        override val utmMedium = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_MEDIUM)
        override val utmCampaign = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CAMPAIGN)
        override val utmContent = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_CONTENT)
        override val utmTerm = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.UTM_TERM)
        override val origin = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.ORIGIN)
        override val path = tracker.subfield(EventTrackerModel.EVENT_TRACKER_MODEL.PATH)
    }
}
