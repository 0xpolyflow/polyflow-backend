package polyflow.config.binding

import mu.KLogging
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import polyflow.config.binding.annotation.ActiveSubscription
import polyflow.config.binding.annotation.DomainLimited
import polyflow.config.binding.annotation.SeatLimited
import polyflow.config.binding.annotation.UserBinding
import polyflow.exception.BadAuthenticationException
import polyflow.exception.NoActiveSubscriptionException
import polyflow.exception.NonExistentUserException
import polyflow.exception.UsageLimitExceededException
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.UserId

class UserResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {

    companion object : KLogging()

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == User::class.java &&
            parameter.hasParameterAnnotation(UserBinding::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): User {
        val requiresActiveSubscription = parameter.hasParameterAnnotation(ActiveSubscription::class.java)
        val isDomainLimited = parameter.hasParameterAnnotation(DomainLimited::class.java)
        val isSeatLimited = parameter.hasParameterAnnotation(SeatLimited::class.java)

        logger.info {
            "Intercepted method call to resolve user, method: ${parameter.method?.name}" +
                " in class: ${parameter.containingClass.name}, requiresActiveSubscription:" +
                " $requiresActiveSubscription, isDomainLimited: $isDomainLimited, isSeatLimited: $isSeatLimited"
        }

        val userId = (SecurityContextHolder.getContext().authentication?.principal as? UserId)
            ?: throw BadAuthenticationException()

        val user = userRepository.getById(userId) ?: throw NonExistentUserException()

        if (requiresActiveSubscription && user.hasNoActiveSubscription()) {
            throw NoActiveSubscriptionException()
        }

        if (isDomainLimited && user.allDomainsUsed()) {
            throw UsageLimitExceededException("number of domains")
        }

        if (isSeatLimited && user.allSeatsUsed()) {
            throw UsageLimitExceededException("number of seats")
        }

        return user
    }

    private fun User.hasNoActiveSubscription() = this.totalDomainLimit == 0 && this.totalSeatLimit == 0
    private fun User.allDomainsUsed() = this.totalDomainLimit >= userRepository.getUsedDomainsById(this.id)
    private fun User.allSeatsUsed() = this.totalSeatLimit >= userRepository.getUsedSeatsById(this.id)
}
