package polyflow.features.alias.model.params

import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias
import polyflow.util.WalletAddress

data class CreateWalletAddressAliasParams(
    val walletAddress: WalletAddress,
    val alias: Alias,
    val projectId: ProjectId
)
