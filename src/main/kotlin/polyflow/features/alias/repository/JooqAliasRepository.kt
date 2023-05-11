package polyflow.features.alias.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import polyflow.features.alias.model.params.CreateSessionIdAliasParams
import polyflow.features.alias.model.params.CreateUserIdAliasParams
import polyflow.features.alias.model.params.CreateWalletAddressAliasParams
import polyflow.features.alias.model.result.ValueWithAlias
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.tables.SessionIdAliasTable
import polyflow.generated.jooq.tables.UserIdAliasTable
import polyflow.generated.jooq.tables.WalletAddressAliasTable
import polyflow.generated.jooq.tables.records.SessionIdAliasRecord
import polyflow.generated.jooq.tables.records.UserIdAliasRecord
import polyflow.generated.jooq.tables.records.WalletAddressAliasRecord
import polyflow.util.Alias
import polyflow.util.WalletAddress

@Repository
class JooqAliasRepository(private val dslContext: DSLContext) : AliasRepository { // TODO test

    companion object : KLogging()

    override fun createForWalletAddress(params: CreateWalletAddressAliasParams): ValueWithAlias<WalletAddress> {
        logger.info { "Create wallet address alias, params: $params" }

        val record = WalletAddressAliasRecord(
            aliasName = params.alias,
            walletAddress = params.walletAddress,
            projectId = params.projectId
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun createForUserId(params: CreateUserIdAliasParams): ValueWithAlias<String> {
        logger.info { "Create user id alias, params: $params" }

        val record = UserIdAliasRecord(
            aliasName = params.alias,
            userId = params.userId,
            projectId = params.projectId
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun createForSessionId(params: CreateSessionIdAliasParams): ValueWithAlias<String> {
        logger.info { "Create session id alias, params: $params" }

        val record = SessionIdAliasRecord(
            aliasName = params.alias,
            sessionId = params.sessionId,
            projectId = params.projectId
        )

        dslContext.executeInsert(record)

        return record.toModel()
    }

    override fun getAllWalletAddressAliasesForProject(projectId: ProjectId): List<ValueWithAlias<WalletAddress>> {
        logger.debug { "Get all wallet address aliases for projectId: $projectId" }

        return dslContext.selectFrom(WalletAddressAliasTable)
            .where(WalletAddressAliasTable.PROJECT_ID.eq(projectId))
            .fetch { it.toModel() }
    }

    override fun getAllUserIdAliasesForProject(projectId: ProjectId): List<ValueWithAlias<String>> {
        logger.debug { "Get all user id aliases for projectId: $projectId" }

        return dslContext.selectFrom(UserIdAliasTable)
            .where(UserIdAliasTable.PROJECT_ID.eq(projectId))
            .fetch { it.toModel() }
    }

    override fun getAllSessionIdAliasesForProject(projectId: ProjectId): List<ValueWithAlias<String>> {
        logger.debug { "Get all session id aliases for projectId: $projectId" }

        return dslContext.selectFrom(SessionIdAliasTable)
            .where(SessionIdAliasTable.PROJECT_ID.eq(projectId))
            .fetch { it.toModel() }
    }

    override fun getForWalletAddress(projectId: ProjectId, alias: Alias): ValueWithAlias<WalletAddress>? {
        logger.debug { "Get wallet address alias, projectId: $projectId, alias: $alias" }

        return dslContext.selectFrom(WalletAddressAliasTable)
            .where(
                DSL.and(
                    WalletAddressAliasTable.ALIAS_NAME.eq(alias),
                    WalletAddressAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .fetchOne { it.toModel() }
    }

    override fun getForUserId(projectId: ProjectId, alias: Alias): ValueWithAlias<String>? {
        logger.debug { "Get user id alias, projectId: $projectId, alias: $alias" }

        return dslContext.selectFrom(UserIdAliasTable)
            .where(
                DSL.and(
                    UserIdAliasTable.ALIAS_NAME.eq(alias),
                    UserIdAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .fetchOne { it.toModel() }
    }

    override fun getForSessionId(projectId: ProjectId, alias: Alias): ValueWithAlias<String>? {
        logger.debug { "Get session id alias, projectId: $projectId, alias: $alias" }

        return dslContext.selectFrom(SessionIdAliasTable)
            .where(
                DSL.and(
                    SessionIdAliasTable.ALIAS_NAME.eq(alias),
                    SessionIdAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .fetchOne { it.toModel() }
    }

    override fun deleteForWalletAddress(projectId: ProjectId, alias: Alias) {
        logger.info { "Delete wallet address alias, projectId: $projectId, alias: $alias" }

        dslContext.deleteFrom(WalletAddressAliasTable)
            .where(
                DSL.and(
                    WalletAddressAliasTable.ALIAS_NAME.eq(alias),
                    WalletAddressAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .execute()
    }

    override fun deleteForUserId(projectId: ProjectId, alias: Alias) {
        logger.info { "Delete user id alias, projectId: $projectId, alias: $alias" }

        dslContext.deleteFrom(UserIdAliasTable)
            .where(
                DSL.and(
                    UserIdAliasTable.ALIAS_NAME.eq(alias),
                    UserIdAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .execute()
    }

    override fun deleteForSessionId(projectId: ProjectId, alias: Alias) {
        logger.info { "Delete session id alias, projectId: $projectId, alias: $alias" }

        dslContext.deleteFrom(SessionIdAliasTable)
            .where(
                DSL.and(
                    SessionIdAliasTable.ALIAS_NAME.eq(alias),
                    SessionIdAliasTable.PROJECT_ID.eq(projectId)
                )
            )
            .execute()
    }

    private fun WalletAddressAliasRecord.toModel() =
        ValueWithAlias(
            value = walletAddress,
            alias = aliasName,
            projectId = projectId
        )

    private fun UserIdAliasRecord.toModel() =
        ValueWithAlias(
            value = userId,
            alias = aliasName,
            projectId = projectId
        )

    private fun SessionIdAliasRecord.toModel() =
        ValueWithAlias(
            value = sessionId,
            alias = aliasName,
            projectId = projectId
        )
}
