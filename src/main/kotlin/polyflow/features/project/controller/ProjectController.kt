package polyflow.features.project.controller

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import polyflow.config.binding.annotation.ActiveSubscription
import polyflow.config.binding.annotation.DomainLimited
import polyflow.config.binding.annotation.SeatLimited
import polyflow.config.binding.annotation.UserBinding
import polyflow.features.project.model.request.CreateProjectRequest
import polyflow.features.project.model.request.ProjectAccessRequest
import polyflow.features.project.model.request.ProjectDomainRequest
import polyflow.features.project.model.request.UpdateProjectFeaturesRequest
import polyflow.features.project.model.response.ProjectResponse
import polyflow.features.project.model.response.ProjectsResponse
import polyflow.features.project.model.response.UserProjectAccessesResponse
import polyflow.features.project.model.response.UserWithAccessResponse
import polyflow.features.project.service.ProjectService
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.id.ProjectId
import javax.validation.Valid

@Validated
@RestController
@Suppress("TooManyFunctions")
class ProjectController(private val projectService: ProjectService) { // TODO test

    @PostMapping("/v1/projects")
    fun create(
        @UserBinding @ActiveSubscription user: User,
        @Valid @RequestBody requestBody: CreateProjectRequest
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.create(user, requestBody)))

    @GetMapping("/v1/projects/{id}")
    fun getById(
        @UserBinding user: User,
        @PathVariable id: ProjectId
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.getById(user, id)))

    @GetMapping("/v1/projects")
    fun getAll(@UserBinding user: User): ResponseEntity<ProjectsResponse> =
        ResponseEntity.ok(ProjectsResponse(projectService.getAllForUser(user).map { ProjectResponse(it) }))

    @PatchMapping("/v1/projects/{id}/features")
    fun updateFeatures(
        @UserBinding user: User,
        @PathVariable id: ProjectId,
        @Valid @RequestBody requestBody: UpdateProjectFeaturesRequest
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.updateFeatures(user, id, requestBody)))

    @PatchMapping("/v1/projects/{id}/domains/add")
    fun addWhitelistedDomain(
        @UserBinding @DomainLimited user: User,
        @PathVariable id: ProjectId,
        @Valid @RequestBody requestBody: ProjectDomainRequest
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.addWhitelistedDomain(user, id, requestBody.domain)))

    @PatchMapping("/v1/projects/{id}/domains/delete")
    fun removeWhitelistedDomain(
        @UserBinding user: User,
        @PathVariable id: ProjectId,
        @Valid @RequestBody requestBody: ProjectDomainRequest
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.removeWhitelistedDomain(user, id, requestBody.domain)))

    @PostMapping("/v1/projects/{id}/api-key")
    fun generateApiKey(
        @UserBinding user: User,
        @PathVariable id: ProjectId
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.generateApiKey(user, id)))

    @DeleteMapping("/v1/projects/{id}/api-key")
    fun deleteApiKey(
        @UserBinding user: User,
        @PathVariable id: ProjectId
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.ok(ProjectResponse(projectService.deleteApiKey(user, id)))

    @PostMapping("/v1/projects/{id}/set-access")
    fun setAccess(
        @UserBinding @SeatLimited user: User, // TODO with full seat limit reached, user will not be able to change access
        @PathVariable id: ProjectId,
        @Valid @RequestBody requestBody: ProjectAccessRequest
    ) {
        projectService.setAccess(user, id, requestBody)
    }

    @PostMapping("/v1/projects/{id}/remove-access")
    fun removeAccess(
        @UserBinding user: User,
        @PathVariable id: ProjectId,
        @Valid @RequestBody requestBody: ProjectAccessRequest
    ) {
        projectService.removeAccess(user, id, requestBody)
    }

    @GetMapping("/v1/projects/{id}/users")
    fun listUsersWithProjectAccess(
        @UserBinding user: User,
        @PathVariable id: ProjectId
    ): ResponseEntity<UserProjectAccessesResponse> =
        ResponseEntity.ok(
            UserProjectAccessesResponse(
                projectService.listUsersWithProjectAccess(user, id).map {
                    UserWithAccessResponse(
                        it.first.id,
                        it.first.email,
                        it.second
                    )
                }
            )
        )
}
