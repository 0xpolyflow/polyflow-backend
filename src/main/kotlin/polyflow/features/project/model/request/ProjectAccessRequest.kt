package polyflow.features.project.model.request

import java.util.UUID
import javax.validation.constraints.NotNull

data class ProjectAccessRequest(
    @field:NotNull
    val userId: UUID
)
