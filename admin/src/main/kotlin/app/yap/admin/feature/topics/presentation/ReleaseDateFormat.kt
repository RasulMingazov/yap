package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.shortName
import java.time.LocalDate

internal object ReleaseDateFormat {

    private val LONG_MONTHS = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря",
    )
    private val LONG_WEEKDAYS = listOf(
        "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
    )
    private val WEEKDAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    fun long(date: LocalDate): String {
        val weekday = LONG_WEEKDAYS[date.dayOfWeek.value - 1]
        val month = LONG_MONTHS[date.monthValue - 1]
        return "$weekday, ${date.dayOfMonth} $month"
    }

    fun short(date: LocalDate): String {
        val weekday = WEEKDAYS[date.dayOfWeek.value - 1]
        val month = date.month.shortName()
        return "$weekday, ${date.dayOfMonth} $month"
    }
}
