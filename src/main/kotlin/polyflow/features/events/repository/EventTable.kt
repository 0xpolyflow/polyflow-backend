package polyflow.features.events.repository

import org.jooq.Field
import org.jooq.Record
import org.jooq.TableField
import org.jooq.impl.TableImpl
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.udt.records.DeviceStateRecord
import polyflow.generated.jooq.udt.records.EventTrackerModelRecord
import polyflow.generated.jooq.udt.records.NetworkStateRecord
import polyflow.generated.jooq.udt.records.ScreenStateRecord
import polyflow.generated.jooq.udt.records.WalletStateRecord
import polyflow.util.ChainId
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress
import java.math.BigInteger
import java.util.UUID

interface EventTable<R : Record, T : TableImpl<R>> {
    val db: T

    val id: TableField<R, UUID>
    val projectId: TableField<R, ProjectId>
    val createdAt: TableField<R, UtcDateTime>

    val tracker: TableField<R, EventTrackerModelRecord>
    val wallet: TableField<R, out WalletStateRecord?>
    val device: TableField<R, DeviceStateRecord>
    val network: TableField<R, out NetworkStateRecord?>

    val walletAddress: Field<WalletAddress>

    val os: Field<String?>
    val browser: Field<String?>
    val country: Field<String?>
    val screen: Field<ScreenStateRecord?>
    val walletProvider: Field<String>
    val walletType: Field<String>

    val eventTracker: Field<String>
    val sessionId: Field<String>
    val userId: Field<String>
    val utmSource: Field<String?>
    val utmMedium: Field<String?>
    val utmCampaign: Field<String?>
    val utmContent: Field<String?>
    val utmTerm: Field<String?>
    val origin: Field<String?>
    val path: Field<String?>
    val query: Field<String?>
    val referrer: Field<String?>

    val chainId: Field<ChainId?>
    val gasPrice: Field<BigInteger?>
    val blockHeight: Field<BigInteger?>
}
