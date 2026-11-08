package app.yap.admin.feature.topics.domain

import java.time.LocalDate

internal data class CalendarRow(
    val date: LocalDate,
    val topic: CalendarTopic?,
)
