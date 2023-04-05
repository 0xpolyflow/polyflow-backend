package polyflow.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import polyflow.generated.jooq.id.GeneratedIdsJacksonModule
import java.math.BigDecimal
import java.math.BigInteger

@Configuration
class JsonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        mapper.propertyNamingStrategy = PropertyNamingStrategies.LowerCamelCaseStrategy()
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(GeneratedIdsJacksonModule)
        mapper.registerModule(ParameterNamesModule())
        mapper.registerModule(
            SimpleModule().apply {
                addSerializer(BigDecimal::class.java, ToStringSerializer())
                addSerializer(BigInteger::class.java, ToStringSerializer())
            }
        )
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return mapper.registerModule(KotlinModule.Builder().build())
    }
}
