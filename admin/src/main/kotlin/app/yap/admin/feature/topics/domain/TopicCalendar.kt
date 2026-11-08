package app.yap.admin.feature.topics.domain

internal data class TopicCalendar(
    val rows: List<CalendarRow>,
    val todos: List<Todo>,
)
