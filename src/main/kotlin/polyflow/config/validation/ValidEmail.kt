package polyflow.config.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Pattern(regexp = "^[^@]+@[^@]+\\.[^@]+$")
@ReportAsSingleViolation
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class ValidEmail(
    val message: String = "value must be a valid email address",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
