package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class CalendarRowDto(
    val date: String,
    val topic: CalendarTopicDto?,
)
