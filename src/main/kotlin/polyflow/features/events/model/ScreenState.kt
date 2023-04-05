package polyflow.features.events.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

data class ScreenState(
    @field:NotNull
    @field:PositiveOrZero
    val w: Int,

    @field:NotNull
    @field:PositiveOrZero
    val h: Int
)
