package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class TopicCalendarDto(
    val rows: List<CalendarRowDto>,
    val todos: List<TodoDto>,
)
