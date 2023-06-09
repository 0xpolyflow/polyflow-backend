package polyflow.features.events.controller

import org.springframework.security.core.context.SecurityContextHolder
import polyflow.exception.BadAuthenticationException
import polyflow.exception.NoActiveSubscriptionException
import polyflow.exception.NonExistentApiKeyException
import polyflow.exception.NonExistentUserException
import polyflow.features.project.model.result.Project
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.UserId
import java.util.Optional

object Util {
    fun resolveUser(userRepository: UserRepository): User {
        val userId = (SecurityContextHolder.getContext().authentication?.principal as? UserId)
            ?: throw BadAuthenticationException()

        return userRepository.getById(userId) ?: throw NonExistentUserException()
    }

    fun resolveProject(
        projectRepository: ProjectRepository,
        userRepository: UserRepository,
        apiKey: Optional<String>
    ): Project =
        apiKey.map {
            projectRepository.getByApiKey(it)?.apply {
                if (userRepository.getById(ownerId)!!.hasNoActiveSubscription()) {
                    throw NoActiveSubscriptionException()
                }
            }
        }.orElseThrow(::NonExistentApiKeyException)!!

    private fun User.hasNoActiveSubscription() = this.totalDomainLimit == 0 && this.totalSeatLimit == 0
}
