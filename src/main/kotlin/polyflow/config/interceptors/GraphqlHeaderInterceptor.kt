package polyflow.config.interceptors

import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.stereotype.Component
import polyflow.config.CustomHeaders
import reactor.core.publisher.Mono
import java.util.Optional

@Component
class GraphqlHeaderInterceptor : WebGraphQlInterceptor {

    override fun intercept(request: WebGraphQlRequest, chain: WebGraphQlInterceptor.Chain): Mono<WebGraphQlResponse> {
        val apiKey = Optional.ofNullable(request.headers.getFirst(CustomHeaders.API_KEY_HEADER))

        request.configureExecutionInput { _, builder ->
            builder.graphQLContext(mapOf("apiKey" to apiKey)).build()
        }

        return chain.next(request)
    }
}
