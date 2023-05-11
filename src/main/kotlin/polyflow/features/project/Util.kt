package polyflow.features.project

import polyflow.exception.AccessForbiddenException
import polyflow.features.project.repository.ProjectRepository
import polyflow.generated.jooq.enums.AccessType
import polyflow.generated.jooq.id.ProjectId
import polyflow.generated.jooq.id.UserId

fun ProjectRepository.requireProjectAccess(userId: UserId, projectId: ProjectId, accessType: AccessType) {
    val hasAccess = when (accessType) {
        AccessType.READ -> this.hasProjectReadAccess(userId, projectId)
        AccessType.WRITE -> this.hasProjectWriteAccess(userId, projectId)
    }

    if (hasAccess.not()) {
        throw AccessForbiddenException(
            "Requesting user does not have access to project with id: ${projectId.value}"
        )
    }
}
