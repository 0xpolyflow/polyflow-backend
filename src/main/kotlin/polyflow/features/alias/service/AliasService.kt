package polyflow.features.alias.service

import polyflow.features.alias.model.request.CreateSessionIdAliasRequest
import polyflow.features.alias.model.request.CreateUserIdAliasRequest
import polyflow.features.alias.model.request.CreateWalletAddressAliasRequest
import polyflow.features.alias.model.result.ValueWithAlias
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.Alias
import polyflow.util.WalletAddress

interface AliasService {
    fun createForWalletAddress(
        userId: UserId,
        projectId: ProjectId,
        request: CreateWalletAddressAliasRequest
    ): ValueWithAlias<WalletAddress>

    fun createForUserId(userId: UserId, projectId: ProjectId, request: CreateUserIdAliasRequest): ValueWithAlias<String>
    fun createForSessionId(
        userId: UserId,
        projectId: ProjectId,
        request: CreateSessionIdAliasRequest
    ): ValueWithAlias<String>

    fun getAllWalletAddressAliasesForProject(userId: UserId, projectId: ProjectId): List<ValueWithAlias<WalletAddress>>
    fun getAllUserIdAliasesForProject(userId: UserId, projectId: ProjectId): List<ValueWithAlias<String>>
    fun getAllSessionIdAliasesForProject(userId: UserId, projectId: ProjectId): List<ValueWithAlias<String>>
    fun getForWalletAddress(userId: UserId, projectId: ProjectId, alias: Alias): ValueWithAlias<WalletAddress>
    fun getForUserId(userId: UserId, projectId: ProjectId, alias: Alias): ValueWithAlias<String>
    fun getForSessionId(userId: UserId, projectId: ProjectId, alias: Alias): ValueWithAlias<String>
    fun deleteForWalletAddress(userId: UserId, projectId: ProjectId, alias: Alias)
    fun deleteForUserId(userId: UserId, projectId: ProjectId, alias: Alias)
    fun deleteForSessionId(userId: UserId, projectId: ProjectId, alias: Alias)
}
