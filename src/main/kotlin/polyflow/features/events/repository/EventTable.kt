package polyflow.features.events.repository

import org.jooq.Field
import org.jooq.Record
import org.jooq.TableField
import org.jooq.impl.TableImpl
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.udt.records.DeviceStateRecord
import polyflow.generated.jooq.udt.records.EventTrackerModelRecord
import polyflow.generated.jooq.udt.records.NetworkStateRecord
import polyflow.generated.jooq.udt.records.WalletStateRecord
import polyflow.util.UtcDateTime
import polyflow.util.WalletAddress

interface EventTable<R : Record, T : TableImpl<R>> {
    val db: T

    val projectId: TableField<R, ProjectId>
    val createdAt: TableField<R, UtcDateTime>

    val eventTracker: TableField<R, EventTrackerModelRecord>
    val wallet: TableField<R, out WalletStateRecord?>
    val device: TableField<R, DeviceStateRecord>
    val network: TableField<R, out NetworkStateRecord?>

    val walletAddress: Field<WalletAddress>
    val walletProvider: Field<String>
    val country: Field<String?>
    val browser: Field<String?>
}
