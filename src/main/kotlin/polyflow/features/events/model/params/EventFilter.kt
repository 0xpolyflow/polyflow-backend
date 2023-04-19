package polyflow.features.events.model.params

import org.jooq.Condition
import org.jooq.impl.DSL
import polyflow.features.events.model.request.filter.DeviceStateFilter
import polyflow.features.events.model.request.filter.EventTrackerModelFilter
import polyflow.features.events.model.request.filter.NetworkStateFilter
import polyflow.features.events.model.request.filter.WalletStateFilter
import polyflow.features.events.repository.EventTable
import polyflow.features.events.repository.subfield
import polyflow.util.ChainId
import polyflow.util.WalletAddress
import polyflow.generated.jooq.udt.DeviceState as DS
import polyflow.generated.jooq.udt.EventTrackerModel as ETM
import polyflow.generated.jooq.udt.NetworkState as NS
import polyflow.generated.jooq.udt.ScreenState as SCST
import polyflow.generated.jooq.udt.WalletState as WS

data class EventFilter(
    val tracker: EventTrackerModelFilter?,
    val wallet: WalletStateFilter?,
    val device: DeviceStateFilter?,
    val network: NetworkStateFilter?
) {
    fun createCondition(eventTable: EventTable<*, *>): Condition? {
        val etm = ETM.EVENT_TRACKER_MODEL
        val ws = WS.WALLET_STATE
        val ds = DS.DEVICE_STATE
        val scst = SCST.SCREEN_STATE
        val ns = NS.NETWORK_STATE

        val conditions = listOfNotNull(
            tracker?.eventTracker?.let { eventTable.tracker.subfield(etm.EVENT_TRACKER).eq(it) },
            tracker?.userId?.let { eventTable.tracker.subfield(etm.USER_ID).eq(it) },
            tracker?.sessionId?.let { eventTable.tracker.subfield(etm.SESSION_ID).eq(it) },
            tracker?.utmSource?.let { eventTable.tracker.subfield(etm.UTM_SOURCE).eq(it) },
            tracker?.utmMedium?.let { eventTable.tracker.subfield(etm.UTM_MEDIUM).eq(it) },
            tracker?.utmCampaign?.let { eventTable.tracker.subfield(etm.UTM_CAMPAIGN).eq(it) },
            tracker?.utmContent?.let { eventTable.tracker.subfield(etm.UTM_CONTENT).eq(it) },
            tracker?.utmTerm?.let { eventTable.tracker.subfield(etm.UTM_TERM).eq(it) },
            tracker?.origin?.let { eventTable.tracker.subfield(etm.ORIGIN).eq(it) },
            tracker?.path?.let { eventTable.tracker.subfield(etm.PATH).eq(it) },

            wallet?.walletAddress?.let { eventTable.wallet.subfield(ws.WALLET_ADDRESS).eq(WalletAddress(it)) },
            wallet?.gasBalance?.let { eventTable.wallet.subfield(ws.GAS_BALANCE).eq(it) },
            wallet?.nonce?.let { eventTable.wallet.subfield(ws.NONCE).eq(it) },
            wallet?.networkId?.let { eventTable.wallet.subfield(ws.NETWORK_ID).eq(it) },

            device?.os?.let { eventTable.device.subfield(ds.OS).eq(it) },
            device?.browser?.let { eventTable.device.subfield(ds.BROWSER).eq(it) },
            device?.country?.let { eventTable.device.subfield(ds.COUNTRY).eq(it) },
            device?.screen?.w?.let { eventTable.device.subfield(ds.SCREEN, scst.W).eq(it) },
            device?.screen?.h?.let { eventTable.device.subfield(ds.SCREEN, scst.H).eq(it) },
            device?.walletProvider?.let { eventTable.device.subfield(ds.WALLET_PROVIDER).eq(it) },
            device?.walletType?.let { eventTable.device.subfield(ds.WALLET_TYPE).eq(it) },

            network?.chainId?.let { eventTable.network.subfield(ns.CHAIN_ID).eq(ChainId(it)) },
            network?.gasPrice?.let { eventTable.network.subfield(ns.GAS_PRICE).eq(it) },
            network?.blockHeight?.let { eventTable.network.subfield(ns.BLOCK_HEIGHT).eq(it) }
        )

        return if (conditions.isNotEmpty()) {
            DSL.and(conditions)
        } else {
            null
        }
    }
}
