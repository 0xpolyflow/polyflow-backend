package polyflow.features.project.repository

import polyflow.features.project.model.params.CreateProjectParams
import polyflow.features.project.model.params.UpdateProjectFeaturesParams
import polyflow.features.project.model.result.Project
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId

@Suppress("TooManyFunctions")
interface ProjectRepository {
    fun create(params: CreateProjectParams): Project
    fun getById(id: ProjectId): Project?
    fun getByApiKey(apiKey: String): Project?
    fun getAllByUserId(userId: UserId): List<Project>
    fun updateFeatures(project: Project, params: UpdateProjectFeaturesParams): Project
    fun addWhitelistedDomain(project: Project, domain: String): Project
    fun removeWhitelistedDomain(project: Project, domain: String): Project
    fun setApiKey(project: Project, apiKey: String): Project
    fun deleteApiKey(project: Project): Project
    fun hasProjectReadAccess(userId: UserId, projectId: ProjectId): Boolean
    fun hasProjectWriteAccess(userId: UserId, projectId: ProjectId): Boolean
    fun setAccess(userId: UserId, projectId: ProjectId, accessType: AccessType)
    fun removeAccess(userId: UserId, projectId: ProjectId)
    fun listUsersWithProjectAccess(projectId: ProjectId): List<Pair<User, AccessType>>
}
