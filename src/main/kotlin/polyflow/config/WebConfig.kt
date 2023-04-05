package polyflow.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import polyflow.config.binding.ProjectApiKeyResolver
import polyflow.config.binding.UserResolver
import polyflow.config.interceptors.CorrelationIdInterceptor
import polyflow.features.project.repository.ProjectRepository
import polyflow.features.user.repository.UserRepository
import polyflow.util.UuidProvider

@Configuration
class WebConfig(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val uuidProvider: UuidProvider
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(UserResolver(userRepository))
        resolvers.add(ProjectApiKeyResolver(projectRepository))
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(CorrelationIdInterceptor(uuidProvider))
    }
}
