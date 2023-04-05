package polyflow.config.binding

import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import polyflow.config.binding.annotation.UserBinding
import polyflow.exception.BadAuthenticationException
import polyflow.exception.NonExistentUserException
import polyflow.features.user.model.result.User
import polyflow.features.user.repository.UserRepository
import polyflow.generated.jooq.id.UserId

class UserResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {

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
        val userId = (SecurityContextHolder.getContext().authentication?.principal as? UserId)
            ?: throw BadAuthenticationException()

        return userRepository.getById(userId) ?: throw NonExistentUserException()
    }
}
