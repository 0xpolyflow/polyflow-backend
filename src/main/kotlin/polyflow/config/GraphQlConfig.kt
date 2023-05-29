package polyflow.config

import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.method.annotation.support.AnnotatedControllerConfigurer
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import polyflow.config.graphql.DurationScalar

@Configuration
class GraphQlConfig : AnnotatedControllerConfigurer() {

    @Bean
    fun runtimeWriterConfigurer(): RuntimeWiringConfigurer =
        RuntimeWiringConfigurer {
            it.scalar(ExtendedScalars.GraphQLLong)
            it.scalar(ExtendedScalars.GraphQLBigInteger)
            it.scalar(ExtendedScalars.GraphQLBigDecimal)
            it.scalar(ExtendedScalars.UUID)
            it.scalar(ExtendedScalars.DateTime)
            it.scalar(
                GraphQLScalarType.newScalar()
                    .name("Duration")
                    .description(
                        "Duration Scalar, either exact or inexact (monthly, yearly).\n" +
                            "Exact format examples:\n" +
                            "- `1d 2h 3m 5s`\n" +
                            "- `7d`\n" +
                            "- `1h 1s`\n" +
                            "Inexact durations:\n" +
                            " - `MONTHLY`\n" +
                            " - `YEARLY`"
                    )
                    .coercing(DurationScalar)
                    .build()
            )
        }
}
