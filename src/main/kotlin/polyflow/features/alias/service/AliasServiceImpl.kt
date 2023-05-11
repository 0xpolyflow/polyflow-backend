package polyflow.features.alias.service

import mu.KLogging
import org.springframework.stereotype.Service
import polyflow.exception.AliasAlreadyExistsException
import polyflow.exception.ResourceNotFoundException
import polyflow.features.alias.model.params.CreateSessionIdAliasParams
import polyflow.features.alias.model.params.CreateUserIdAliasParams
import polyflow.features.alias.model.params.CreateWalletAddressAliasParams
import polyflow.features.alias.model.request.CreateSessionIdAliasRequest
import polyflow.features.alias.model.request.CreateUserIdAliasRequest
import polyflow.features.alias.model.request.CreateWalletAddressAliasRequest
import polyflow.features.alias.model.result.ValueWithAlias
import polyflow.features.alias.repository.AliasRepository
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.project.requireProjectAccess
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.Alias
import polyflow.util.WalletAddress

@Service
class AliasServiceImpl(
    private val aliasRepository: AliasRepository,
    private val projectRepository: ProjectRepository
) : AliasService { // TODO test

    companion object : KLogging()

    override fun createForWalletAddress(
        userId: UserId,
        projectId: ProjectId,
        request: CreateWalletAddressAliasRequest
    ): ValueWithAlias<WalletAddress> {
        logger.info { "Create wallet address alias, userId: $userId, projectId: $projectId, request: $request" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        val alias = Alias(request.alias)

        if (aliasRepository.getForWalletAddress(projectId, alias) != null) {
            throw AliasAlreadyExistsException("Wallet address", alias, projectId)
        }

        return aliasRepository.createForWalletAddress(
            CreateWalletAddressAliasParams(
                walletAddress = WalletAddress(request.walletAddress),
                alias = alias,
                projectId = projectId
            )
        )
    }

    override fun createForUserId(
        userId: UserId,
        projectId: ProjectId,
        request: CreateUserIdAliasRequest
    ): ValueWithAlias<String> {
        logger.info { "Create user id alias, userId: $userId, projectId: $projectId, request: $request" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        val alias = Alias(request.alias)

        if (aliasRepository.getForUserId(projectId, alias) != null) {
            throw AliasAlreadyExistsException("User ID", alias, projectId)
        }

        return aliasRepository.createForUserId(
            CreateUserIdAliasParams(
                userId = request.userId,
                alias = alias,
                projectId = projectId
            )
        )
    }

    override fun createForSessionId(
        userId: UserId,
        projectId: ProjectId,
        request: CreateSessionIdAliasRequest
    ): ValueWithAlias<String> {
        logger.info { "Create session id alias, userId: $userId, projectId: $projectId, request: $request" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        val alias = Alias(request.alias)

        if (aliasRepository.getForSessionId(projectId, alias) != null) {
            throw AliasAlreadyExistsException("Session ID", alias, projectId)
        }

        return aliasRepository.createForSessionId(
            CreateSessionIdAliasParams(
                sessionId = request.sessionId,
                alias = alias,
                projectId = projectId
            )
        )
    }

    override fun getAllWalletAddressAliasesForProject(
        userId: UserId,
        projectId: ProjectId
    ): List<ValueWithAlias<WalletAddress>> {
        logger.debug { "Get all wallet address aliases for project, userId: $userId, projectId: $projectId" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getAllWalletAddressAliasesForProject(projectId)
    }

    override fun getAllUserIdAliasesForProject(userId: UserId, projectId: ProjectId): List<ValueWithAlias<String>> {
        logger.debug { "Get all user id aliases for project, userId: $userId, projectId: $projectId" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getAllUserIdAliasesForProject(projectId)
    }

    override fun getAllSessionIdAliasesForProject(userId: UserId, projectId: ProjectId): List<ValueWithAlias<String>> {
        logger.debug { "Get all session id aliases for project, userId: $userId, projectId: $projectId" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getAllSessionIdAliasesForProject(projectId)
    }

    override fun getForWalletAddress(
        userId: UserId,
        projectId: ProjectId,
        alias: Alias
    ): ValueWithAlias<WalletAddress> {
        logger.debug { "Get wallet address alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getForWalletAddress(projectId, alias)
            ?: throw ResourceNotFoundException("Wallet address alias not found: ${alias.value}")
    }

    override fun getForUserId(userId: UserId, projectId: ProjectId, alias: Alias): ValueWithAlias<String> {
        logger.debug { "Get user id alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getForUserId(projectId, alias)
            ?: throw ResourceNotFoundException("User ID alias not found: ${alias.value}")
    }

    override fun getForSessionId(userId: UserId, projectId: ProjectId, alias: Alias): ValueWithAlias<String> {
        logger.debug { "Get session id alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.READ)

        return aliasRepository.getForSessionId(projectId, alias)
            ?: throw ResourceNotFoundException("Session ID alias not found: ${alias.value}")
    }

    override fun deleteForWalletAddress(userId: UserId, projectId: ProjectId, alias: Alias) {
        logger.info { "Delete wallet address alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        aliasRepository.deleteForWalletAddress(projectId, alias)
    }

    override fun deleteForUserId(userId: UserId, projectId: ProjectId, alias: Alias) {
        logger.info { "Delete user id alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        aliasRepository.deleteForUserId(projectId, alias)
    }

    override fun deleteForSessionId(userId: UserId, projectId: ProjectId, alias: Alias) {
        logger.info { "Delete session id alias for project, userId: $userId, projectId: $projectId, alias: $alias" }

        projectRepository.requireProjectAccess(userId, projectId, AccessType.WRITE)

        aliasRepository.deleteForSessionId(projectId, alias)
    }
}
