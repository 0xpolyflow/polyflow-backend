package polyflow.config.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Pattern(regexp = "^(0x)?[A-Fa-f0-9]*$")
@ReportAsSingleViolation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class ValidHexString(
    val message: String = "value must be valid Ethereum function data",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
