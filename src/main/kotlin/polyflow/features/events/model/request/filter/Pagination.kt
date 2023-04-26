package polyflow.features.events.model.request.filter

import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

data class Pagination(
    @field:NotNull
    @field:PositiveOrZero
    val limit: Int,

    @field:NotNull
    @field:PositiveOrZero
    val offset: Int
)
