package polyflow.features.alias.repository

import polyflow.features.alias.model.params.CreateSessionIdAliasParams
import polyflow.features.alias.model.params.CreateUserIdAliasParams
import polyflow.features.alias.model.params.CreateWalletAddressAliasParams
import polyflow.features.alias.model.result.ValueWithAlias
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias
import polyflow.util.WalletAddress

interface AliasRepository {
    fun createForWalletAddress(params: CreateWalletAddressAliasParams): ValueWithAlias<WalletAddress>
    fun createForUserId(params: CreateUserIdAliasParams): ValueWithAlias<String>
    fun createForSessionId(params: CreateSessionIdAliasParams): ValueWithAlias<String>
    fun getAllWalletAddressAliasesForProject(projectId: ProjectId): List<ValueWithAlias<WalletAddress>>
    fun getAllUserIdAliasesForProject(projectId: ProjectId): List<ValueWithAlias<String>>
    fun getAllSessionIdAliasesForProject(projectId: ProjectId): List<ValueWithAlias<String>>
    fun getForWalletAddress(projectId: ProjectId, alias: Alias): ValueWithAlias<WalletAddress>?
    fun getForUserId(projectId: ProjectId, alias: Alias): ValueWithAlias<String>?
    fun getForSessionId(projectId: ProjectId, alias: Alias): ValueWithAlias<String>?
    fun deleteForWalletAddress(projectId: ProjectId, alias: Alias)
    fun deleteForUserId(projectId: ProjectId, alias: Alias)
    fun deleteForSessionId(projectId: ProjectId, alias: Alias)
}
