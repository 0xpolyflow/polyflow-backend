package polyflow.config.converters

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import polyflow.util.ChainId

@Component
@ConfigurationPropertiesBinding
class StringToChainIdConverter : Converter<String, ChainId> {
    override fun convert(source: String): ChainId = ChainId(source.toLong())
}
