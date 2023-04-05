package polyflow.config.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Size
import kotlin.reflect.KClass

@Size(max = ValidationConstants.REQUEST_BODY_MAX_LIST_SIZE)
@ReportAsSingleViolation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class MaxListSize(
    val message: String = "size must be between 0 and ${ValidationConstants.REQUEST_BODY_MAX_LIST_SIZE}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
