package polyflow.features.project.service

import polyflow.features.project.model.request.CreateProjectRequest
import polyflow.features.project.model.request.ProjectAccessRequest
import polyflow.features.project.model.request.UpdateProjectFeaturesRequest
import polyflow.features.project.model.result.Project
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectId

@Suppress("TooManyFunctions")
interface ProjectService {
    fun create(user: User, request: CreateProjectRequest): Project
    fun getById(user: User, id: ProjectId): Project
    fun getAllForUser(user: User): List<Project>
    fun updateFeatures(user: User, id: ProjectId, request: UpdateProjectFeaturesRequest): Project
    fun addWhitelistedDomain(user: User, id: ProjectId, domain: String): Project
    fun removeWhitelistedDomain(user: User, id: ProjectId, domain: String): Project
    fun generateApiKey(user: User, id: ProjectId): Project
    fun deleteApiKey(user: User, id: ProjectId): Project
    fun setAccess(user: User, projectId: ProjectId, request: ProjectAccessRequest)
    fun removeAccess(user: User, projectId: ProjectId, request: ProjectAccessRequest)
    fun listUsersWithProjectAccess(user: User, projectId: ProjectId): List<Pair<User, AccessType>>
}
