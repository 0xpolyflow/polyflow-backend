package polyflow.features.project.service

import mu.KLogging
import org.springframework.stereotype.Service
import polyflow.exception.ResourceNotFoundException
import polyflow.features.project.model.params.CreateProjectParams
import polyflow.features.project.model.params.UpdateProjectFeaturesParams
import polyflow.features.project.model.request.CreateProjectRequest
import polyflow.features.project.model.request.ProjectAccessRequest
import polyflow.features.project.model.request.UpdateProjectFeaturesRequest
import polyflow.features.project.model.result.Project
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectFeaturesId
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId
import polyflow.util.RandomProvider
import polyflow.util.UtcDateTimeProvider
import polyflow.util.UuidProvider
import java.util.Base64

@Service
@Suppress("TooManyFunctions")
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val uuidProvider: UuidProvider,
    private val utcDateTimeProvider: UtcDateTimeProvider,
    private val randomProvider: RandomProvider
) : ProjectService { // TODO test

    companion object : KLogging() {
        private const val API_KEY_BYTES = 33
        private const val API_KEY_PREFIX_LENGTH = 5
        private val ENCODER = Base64.getUrlEncoder()
    }

    override fun create(user: User, request: CreateProjectRequest): Project {
        logger.info { "Creating project, userId: ${user.id}, request: $request" }

        val params = CreateProjectParams(
            id = uuidProvider.getUuid(ProjectId),
            name = request.name,
            ownerId = user.id,
            createdAt = utcDateTimeProvider.getUtcDateTime(),
            featuresId = uuidProvider.getUuid(ProjectFeaturesId)
        )

        return projectRepository.create(params)
    }

    override fun getById(user: User, id: ProjectId): Project {
        logger.debug { "Get project by id, userId: ${user.id}, projectId: $id" }
        return getByIdWithAccess(id, user.id, AccessType.READ)
    }

    override fun getAllForUser(user: User): List<Project> {
        logger.debug { "Get all projects for user, userId: ${user.id}" }

        return projectRepository.getAllByUserId(user.id)
    }

    override fun updateFeatures(user: User, id: ProjectId, request: UpdateProjectFeaturesRequest): Project {
        logger.info { "Update project features, userId: ${user.id}, projectId: $id, request: $request" }

        val project = getByIdWithAccess(id, user.id, AccessType.WRITE)
        val params = UpdateProjectFeaturesParams(
            gasStation = request.gasStation,
            networkSwitcher = request.networkSwitcher,
            connectWallet = request.connectWallet,
            compliance = request.compliance,
            errorMessages = request.errorMessages
        )

        return projectRepository.updateFeatures(project, params)
    }

    override fun addWhitelistedDomain(user: User, id: ProjectId, domain: String): Project {
        logger.info { "Add domain to project whitelist, userId: ${user.id}, projectId: $id, domain: $domain" }

        val project = getByIdWithAccess(id, user.id, AccessType.WRITE)

        return projectRepository.addWhitelistedDomain(project, domain)
    }

    override fun removeWhitelistedDomain(user: User, id: ProjectId, domain: String): Project {
        logger.info { "Remove domain from project whitelist, userId: ${user.id}, projectId: $id, domain: $domain" }

        val project = getByIdWithAccess(id, user.id, AccessType.WRITE)

        return projectRepository.removeWhitelistedDomain(project, domain)
    }

    override fun generateApiKey(user: User, id: ProjectId): Project {
        logger.info { "Generate API key for project, userId: ${user.id}, projectId: $id" }

        val project = getByIdWithAccess(id, user.id, AccessType.WRITE)
        val apiKeyBytes = randomProvider.getBytes(API_KEY_BYTES)
        val encodedApiKey = ENCODER.encodeToString(apiKeyBytes)
        val apiKey = "${encodedApiKey.take(API_KEY_PREFIX_LENGTH)}.${encodedApiKey.drop(API_KEY_PREFIX_LENGTH)}"

        return projectRepository.setApiKey(project, apiKey)
    }

    override fun deleteApiKey(user: User, id: ProjectId): Project {
        logger.info { "Remove API key from project, userId: ${user.id}, projectId: $id" }

        val project = getByIdWithAccess(id, user.id, AccessType.WRITE)

        return projectRepository.deleteApiKey(project)
    }

    override fun setAccess(user: User, projectId: ProjectId, request: ProjectAccessRequest) {
        logger.info { "Set project read access, ownerId: ${user.id}, projectId: $projectId, request: $request" }

        val project = getByIdForOwner(projectId, user.id)

        projectRepository.setAccess(
            userId = UserId(request.userId),
            projectId = project.id,
            accessType = AccessType.READ
        )
    }

    override fun removeAccess(user: User, projectId: ProjectId, request: ProjectAccessRequest) {
        logger.info { "Remove project access, ownerId: ${user.id}, projectId: $projectId, request: $request" }

        val project = getByIdForOwner(projectId, user.id)

        projectRepository.removeAccess(
            userId = UserId(request.userId),
            projectId = project.id
        )
    }

    override fun listUsersWithProjectAccess(user: User, projectId: ProjectId): List<Pair<User, AccessType>> {
        logger.debug { "List all users with access to project, ownerId: ${user.id}, projectId: $projectId" }

        val project = getByIdForOwner(projectId, user.id)

        return projectRepository.listUsersWithProjectAccess(project.id)
    }

    private fun getByIdForOwner(id: ProjectId, ownerId: UserId): Project =
        projectRepository.getById(id)?.takeIf { it.ownerId == ownerId }
            ?: throw ResourceNotFoundException("Project not found for requested id and user")

    private fun getByIdWithAccess(id: ProjectId, userId: UserId, accessType: AccessType): Project =
        projectRepository.getById(id)
            ?.takeIf {
                when (accessType) {
                    AccessType.READ -> projectRepository.hasProjectReadAccess(userId, it.id)
                    AccessType.WRITE -> projectRepository.hasProjectWriteAccess(userId, it.id)
                }
            }
            ?: throw ResourceNotFoundException("Project not found for requested id and user")
}
