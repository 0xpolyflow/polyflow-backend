package polyflow.config.graphql

import graphql.language.StringValue
import graphql.language.Value
import graphql.scalars.util.Kit
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import polyflow.util.Duration
import polyflow.util.ExactDuration
import polyflow.util.InexactDuration
import kotlin.time.Duration as KotlinDuration

object DurationScalar : Coercing<Duration, String> {

    override fun serialize(dataFetcherResult: Any): String =
        when (dataFetcherResult) {
            is ExactDuration -> dataFetcherResult.duration.toString()
            is InexactDuration -> dataFetcherResult.show
            else -> throw CoercingSerializeException(
                "Expected something we can convert to 'polyflow.util.Duration'" +
                    " but was '${Kit.typeName(dataFetcherResult)}'."
            )
        }

    override fun parseValue(input: Any): Duration =
        if (input is String) {
            parseOrThrow(input, ::CoercingParseValueException)
        } else {
            throw CoercingParseValueException("Expected a 'String' but was '${Kit.typeName(input)}'.")
        }

    override fun parseLiteral(input: Any): Duration =
        if (input is StringValue) {
            parseOrThrow(input.value, ::CoercingParseLiteralException)
        } else {
            throw CoercingParseLiteralException("Expected AST type 'StringValue' but was '${Kit.typeName(input)}'.")
        }

    override fun valueToLiteral(input: Any): Value<out Value<*>> {
        return StringValue.newStringValue(serialize(input)).build()
    }

    private fun <E : Throwable> parseOrThrow(value: String, exception: (String) -> E): Duration =
        try {
            InexactDuration.parse(value) ?: ExactDuration(KotlinDuration.parse(value))
        } catch (e: IllegalArgumentException) {
            throw exception("Invalid duration format: '$value'. because of : '${e.message}'.")
        }
}
