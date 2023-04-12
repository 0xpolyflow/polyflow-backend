package polyflow.features.project.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.util.postgres.PostgresDSL
import org.springframework.stereotype.Repository
import polyflow.features.project.model.params.CreateProjectParams
import polyflow.features.project.model.params.UpdateProjectFeaturesParams
import polyflow.features.project.model.result.Project
import polyflow.features.project.model.result.ProjectFeatures
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.generated.jooq.tables.ProjectFeaturesTable
import polyflow.generated.jooq.tables.ProjectTable
import polyflow.generated.jooq.tables.UserProjectAccessTable
import polyflow.generated.jooq.tables.UserTable
import polyflow.generated.jooq.tables.records.ProjectFeaturesRecord
import polyflow.generated.jooq.tables.records.ProjectRecord
import polyflow.generated.jooq.tables.records.UserProjectAccessRecord
import polyflow.generated.jooq.tables.records.UserRecord

@Repository
@Suppress("TooManyFunctions")
class JooqProjectRepository(private val dslContext: DSLContext) : ProjectRepository { // TODO test

    companion object : KLogging()

    override fun create(params: CreateProjectParams): Project {
        logger.info { "Create project: $params" }

        val featuresRecord = ProjectFeaturesRecord(
            id = params.featuresId,
            gasStation = false,
            networkSwitcher = false,
            connectWallet = false,
            compliance = false,
            errorMessages = false
        )

        val projectRecord = ProjectRecord(
            id = params.id,
            name = params.name,
            apiKey = null,
            ownerId = params.ownerId,
            createdAt = params.createdAt,
            whitelistedDomains = emptyArray(),
            featuresId = params.featuresId
        )

        dslContext.executeInsert(featuresRecord)
        dslContext.executeInsert(projectRecord)

        return projectRecord.toModel(featuresRecord)
    }

    override fun getById(id: ProjectId): Project? {
        logger.debug { "Get project by id: $id" }

        return dslContext.select()
            .from(ProjectTable.join(ProjectFeaturesTable).on(ProjectTable.FEATURES_ID.eq(ProjectFeaturesTable.ID)))
            .where(ProjectTable.ID.eq(id))
            .fetchOne {
                val projectRecord = it.into(ProjectTable)
                val featuresRecord = it.into(ProjectFeaturesTable)

                projectRecord.toModel(featuresRecord)
            }
    }

    override fun getByApiKey(apiKey: String): Project? {
        logger.debug { "Get project by apiKey: $apiKey" }

        return dslContext.select()
            .from(ProjectTable.join(ProjectFeaturesTable).on(ProjectTable.FEATURES_ID.eq(ProjectFeaturesTable.ID)))
            .where(ProjectTable.API_KEY.eq(apiKey))
            .fetchOne {
                val projectRecord = it.into(ProjectTable)
                val featuresRecord = it.into(ProjectFeaturesTable)

                projectRecord.toModel(featuresRecord)
            }
    }

    override fun getAllByUserId(userId: UserId): List<Project> {
        logger.debug { "Get project by user id: $userId" }

        val fields = ProjectTable.fields().toList() + ProjectFeaturesTable.fields().toList()

        return dslContext.select(fields)
            .from(
                ProjectTable.join(ProjectFeaturesTable).on(ProjectTable.FEATURES_ID.eq(ProjectFeaturesTable.ID))
                    .fullJoin(UserProjectAccessTable).on(ProjectTable.ID.eq(UserProjectAccessTable.PROJECT_ID))
            )
            .where(
                DSL.or(
                    ProjectTable.OWNER_ID.eq(userId),
                    DSL.and(
                        UserProjectAccessTable.USER_ID.eq(userId),
                        UserProjectAccessTable.ACCESS_TYPE.`in`(AccessType.WRITE, AccessType.READ)
                    )
                )
            )
            .fetch {
                val projectRecord = it.into(ProjectTable)
                val featuresRecord = it.into(ProjectFeaturesTable)

                projectRecord.toModel(featuresRecord)
            }
    }

    override fun updateFeatures(project: Project, params: UpdateProjectFeaturesParams): Project {
        logger.info { "Update project features, projectId: ${project.id}, params: $params" }

        val updatedFeatures = project.features.updateFields(params)

        dslContext.update(ProjectFeaturesTable)
            .set(updatedFeatures.intoMap())
            .where(ProjectFeaturesTable.ID.eq(project.features.id))
            .execute()

        return project.copy(features = updatedFeatures.toModel())
    }

    override fun addWhitelistedDomain(project: Project, domain: String): Project {
        logger.info { "Add whitelisted domain to project, projectId: ${project.id}, domain: $domain" }

        dslContext.update(ProjectTable)
            .set(ProjectTable.WHITELISTED_DOMAINS, PostgresDSL.arrayAppend(ProjectTable.WHITELISTED_DOMAINS, domain))
            .where(ProjectTable.ID.eq(project.id))
            .execute()

        return project.copy(whitelistedDomains = project.whitelistedDomains + domain)
    }

    override fun removeWhitelistedDomain(project: Project, domain: String): Project {
        logger.info { "Remove whitelisted domain from project, projectId: ${project.id}, domain: $domain" }

        dslContext.update(ProjectTable)
            .set(ProjectTable.WHITELISTED_DOMAINS, PostgresDSL.arrayRemove(ProjectTable.WHITELISTED_DOMAINS, domain))
            .where(ProjectTable.ID.eq(project.id))
            .execute()

        return project.copy(whitelistedDomains = project.whitelistedDomains.filterNot { it == domain })
    }

    override fun setApiKey(project: Project, apiKey: String): Project {
        logger.info { "Set API key for project, projectId: ${project.id}, apiKey: $apiKey" }

        dslContext.update(ProjectTable)
            .set(ProjectTable.API_KEY, apiKey)
            .where(ProjectTable.ID.eq(project.id))
            .execute()

        return project.copy(apiKey = apiKey)
    }

    override fun deleteApiKey(project: Project): Project {
        logger.info { "Delete API key for project, projectId: ${project.id}" }

        dslContext.update(ProjectTable)
            .setNull(ProjectTable.API_KEY)
            .where(ProjectTable.ID.eq(project.id))
            .execute()

        return project.copy(apiKey = null)
    }

    override fun hasProjectReadAccess(userId: UserId, projectId: ProjectId): Boolean {
        logger.debug { "Check if user has read access to project, userId: $userId, projectId: $projectId" }
        return hasProjectAccess(userId, projectId, listOf(AccessType.WRITE, AccessType.READ))
    }

    override fun hasProjectWriteAccess(userId: UserId, projectId: ProjectId): Boolean {
        logger.debug { "Check if user has write access to project, userId: $userId, projectId: $projectId" }
        return hasProjectAccess(userId, projectId, listOf(AccessType.WRITE))
    }

    override fun setAccess(userId: UserId, projectId: ProjectId, accessType: AccessType) {
        logger.info { "Set user project access, userId: $userId, projectId: $projectId, accessType: $accessType" }

        dslContext.insertInto(UserProjectAccessTable)
            .set(
                UserProjectAccessRecord(
                    userId = userId,
                    projectId = projectId,
                    accessType = accessType
                )
            )
            .onConflict(UserProjectAccessTable.PROJECT_ID, UserProjectAccessTable.USER_ID)
            .doUpdate()
            .set(UserProjectAccessTable.ACCESS_TYPE, accessType)
            .execute()
    }

    override fun removeAccess(userId: UserId, projectId: ProjectId) {
        logger.info { "Remove user project access, userId: $userId, projectId: $projectId" }

        dslContext.deleteFrom(UserProjectAccessTable)
            .where(
                DSL.and(
                    UserProjectAccessTable.PROJECT_ID.eq(projectId),
                    UserProjectAccessTable.USER_ID.eq(userId)
                )
            )
            .execute()
    }

    override fun listUsersWithProjectAccess(projectId: ProjectId): List<Pair<User, AccessType>> {
        logger.debug { "List users with project access, projectId: $projectId" }

        val fields = UserTable.fields().toList() + UserProjectAccessTable.ACCESS_TYPE

        return dslContext.select(fields)
            .from(UserTable.join(UserProjectAccessTable).on(UserTable.ID.eq(UserProjectAccessTable.USER_ID)))
            .where(UserProjectAccessTable.PROJECT_ID.eq(projectId))
            .fetch { Pair(it.into(UserTable).toModel(), it.get(UserProjectAccessTable.ACCESS_TYPE)) }
    }

    private fun hasProjectAccess(userId: UserId, projectId: ProjectId, accessTypes: List<AccessType>): Boolean {
        return dslContext.fetchExists(
            ProjectTable.fullJoin(UserProjectAccessTable).on(ProjectTable.ID.eq(UserProjectAccessTable.PROJECT_ID)),
            DSL.or(
                DSL.and(
                    ProjectTable.ID.eq(projectId),
                    ProjectTable.OWNER_ID.eq(userId)
                ),
                DSL.and(
                    UserProjectAccessTable.PROJECT_ID.eq(projectId),
                    UserProjectAccessTable.USER_ID.eq(userId),
                    UserProjectAccessTable.ACCESS_TYPE.`in`(accessTypes)
                )
            )
        )
    }

    private fun ProjectFeatures.updateFields(params: UpdateProjectFeaturesParams): ProjectFeaturesRecord =
        ProjectFeaturesRecord(
            id = id,
            gasStation = params.gasStation ?: gasStation,
            networkSwitcher = params.networkSwitcher ?: networkSwitcher,
            connectWallet = params.connectWallet ?: connectWallet,
            compliance = params.compliance ?: compliance,
            errorMessages = params.errorMessages ?: errorMessages
        )

    private fun ProjectFeaturesRecord.toModel(): ProjectFeatures =
        ProjectFeatures(
            id = id,
            gasStation = gasStation,
            networkSwitcher = networkSwitcher,
            connectWallet = connectWallet,
            compliance = compliance,
            errorMessages = errorMessages
        )

    private fun ProjectRecord.toModel(featuresRecord: ProjectFeaturesRecord): Project =
        Project(
            id = id,
            name = name,
            apiKey = apiKey,
            ownerId = ownerId,
            createdAt = createdAt,
            whitelistedDomains = whitelistedDomains.toList(),
            features = featuresRecord.toModel()
        )

    private fun UserRecord.toModel(): User =
        User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            accountType = accountType,
            registeredAt = registeredAt,
            verifiedAt = verifiedAt,
            stripeCustomerId = stripeCustomerId,
            totalDomainLimit = totalDomainLimit,
            totalSeatLimit = totalSeatLimit
        )
}
