package polyflow.config.binding

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import polyflow.config.CustomHeaders
import polyflow.config.binding.annotation.ApiKeyBinding
import polyflow.exception.NonExistentApiKeyException
import polyflow.features.project.model.result.Project
import polyflow.features.project.repository.ProjectRepository
import javax.servlet.http.HttpServletRequest

class ProjectApiKeyResolver(
    private val projectRepository: ProjectRepository
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Project::class.java &&
            parameter.hasParameterAnnotation(ApiKeyBinding::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Project {
        val httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest::class.java)
        return httpServletRequest?.getHeader(CustomHeaders.API_KEY_HEADER)
            ?.let { projectRepository.getByApiKey(it) }
            ?: throw NonExistentApiKeyException()
    }
}
