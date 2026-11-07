package app.yap.admin.feature.topics.presentation

import java.time.LocalDate

internal object ReleaseDateFormat {

    private val MONTHS = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    private val WEEKDAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    fun short(date: LocalDate): String {
        val weekday = WEEKDAYS[date.dayOfWeek.value - 1]
        val month = MONTHS[date.monthValue - 1]
        return "$weekday, ${date.dayOfMonth} $month"
    }
}
