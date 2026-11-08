package app.yap.server.feature.topics.model

data class TopicCalendar(
    val rows: List<CalendarRow>,
    val todos: List<Todo>,
)
