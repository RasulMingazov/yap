package app.yap.server.feature.topics.model

import java.time.LocalDate

data class CalendarRow(
    val date: LocalDate,
    val topic: CalendarTopic?,
)
