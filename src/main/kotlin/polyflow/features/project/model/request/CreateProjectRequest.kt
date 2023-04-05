package polyflow.features.project.model.request

import polyflow.config.validation.MaxStringSize
import javax.validation.constraints.NotNull

data class CreateProjectRequest(
    @field:NotNull
    @field:MaxStringSize
    val name: String
)
