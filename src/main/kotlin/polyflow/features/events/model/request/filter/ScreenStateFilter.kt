package polyflow.features.events.model.request.filter

import javax.validation.constraints.PositiveOrZero

data class ScreenStateFilter(
    @field:PositiveOrZero
    val w: Int?,

    @field:PositiveOrZero
    val h: Int?
)
