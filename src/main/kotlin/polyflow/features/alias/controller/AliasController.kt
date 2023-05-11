package polyflow.features.alias.controller

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import polyflow.config.binding.annotation.UserBinding
import polyflow.features.alias.model.request.CreateSessionIdAliasRequest
import polyflow.features.alias.model.request.CreateUserIdAliasRequest
import polyflow.features.alias.model.request.CreateWalletAddressAliasRequest
import polyflow.features.alias.model.response.ProjectAliasesResponse
import polyflow.features.alias.model.response.SessionIdAliasResponse
import polyflow.features.alias.model.response.UserIdAliasResponse
import polyflow.features.alias.model.response.WalletAddressAliasResponse
import polyflow.features.alias.service.AliasService
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.id.ProjectId
import polyflow.util.Alias
import javax.validation.Valid

@Validated
@RestController
class AliasController(private val aliasService: AliasService) { // TODO test

    @PostMapping("/v1/project-aliases/{projectId}/wallet-address")
    fun create(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @Valid @RequestBody requestBody: CreateWalletAddressAliasRequest
    ): ResponseEntity<WalletAddressAliasResponse> =
        ResponseEntity.ok(
            WalletAddressAliasResponse(
                aliasService.createForWalletAddress(
                    userId = user.id,
                    projectId = projectId,
                    request = requestBody
                )
            )
        )

    @PostMapping("/v1/project-aliases/{projectId}/user-id")
    fun create(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @Valid @RequestBody requestBody: CreateUserIdAliasRequest
    ): ResponseEntity<UserIdAliasResponse> =
        ResponseEntity.ok(UserIdAliasResponse(aliasService.createForUserId(user.id, projectId, requestBody)))

    @PostMapping("/v1/project-aliases/{projectId}/session-id")
    fun create(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @Valid @RequestBody requestBody: CreateSessionIdAliasRequest
    ): ResponseEntity<SessionIdAliasResponse> =
        ResponseEntity.ok(SessionIdAliasResponse(aliasService.createForSessionId(user.id, projectId, requestBody)))

    @GetMapping("/v1/project-aliases/{projectId}/wallet-address")
    fun getAllWalletAddressAliasesForProject(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId
    ): ResponseEntity<ProjectAliasesResponse<WalletAddressAliasResponse>> =
        ResponseEntity.ok(
            ProjectAliasesResponse(
                values = aliasService.getAllWalletAddressAliasesForProject(user.id, projectId)
                    .map(::WalletAddressAliasResponse),
                projectId = projectId
            )
        )

    @GetMapping("/v1/project-aliases/{projectId}/user-id")
    fun getAllUserIdAliasesForProject(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId
    ): ResponseEntity<ProjectAliasesResponse<UserIdAliasResponse>> =
        ResponseEntity.ok(
            ProjectAliasesResponse(
                values = aliasService.getAllUserIdAliasesForProject(user.id, projectId)
                    .map(::UserIdAliasResponse),
                projectId = projectId
            )
        )

    @GetMapping("/v1/project-aliases/{projectId}/session-id")
    fun getAllSessionIdAliasesForProject(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId
    ): ResponseEntity<ProjectAliasesResponse<SessionIdAliasResponse>> =
        ResponseEntity.ok(
            ProjectAliasesResponse(
                values = aliasService.getAllSessionIdAliasesForProject(user.id, projectId)
                    .map(::SessionIdAliasResponse),
                projectId = projectId
            )
        )

    @GetMapping("/v1/project-aliases/{projectId}/wallet-address/{alias}")
    fun getWalletAddressByAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ): ResponseEntity<WalletAddressAliasResponse> =
        ResponseEntity.ok(WalletAddressAliasResponse(aliasService.getForWalletAddress(user.id, projectId, alias)))

    @GetMapping("/v1/project-aliases/{projectId}/user-id/{alias}")
    fun getUserIdByAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ): ResponseEntity<UserIdAliasResponse> =
        ResponseEntity.ok(UserIdAliasResponse(aliasService.getForUserId(user.id, projectId, alias)))

    @GetMapping("/v1/project-aliases/{projectId}/session-id/{alias}")
    fun getSessionIdByAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ): ResponseEntity<SessionIdAliasResponse> =
        ResponseEntity.ok(SessionIdAliasResponse(aliasService.getForSessionId(user.id, projectId, alias)))

    @DeleteMapping("/v1/project-aliases/{projectId}/wallet-address/{alias}")
    fun deleteWalletAddressAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ) {
        aliasService.deleteForWalletAddress(user.id, projectId, alias)
    }

    @DeleteMapping("/v1/project-aliases/{projectId}/user-id/{alias}")
    fun deleteUserIdAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ) {
        aliasService.deleteForUserId(user.id, projectId, alias)
    }

    @DeleteMapping("/v1/project-aliases/{projectId}/session-id/{alias}")
    fun deleteSessionIdAlias(
        @UserBinding user: User,
        @PathVariable projectId: ProjectId,
        @PathVariable alias: Alias
    ) {
        aliasService.deleteForSessionId(user.id, projectId, alias)
    }
}
