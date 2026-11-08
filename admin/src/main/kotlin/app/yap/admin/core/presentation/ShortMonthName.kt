package app.yap.admin.core.presentation

import java.time.Month

private val SHORT_MONTH_NAMES = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")

internal fun Month.shortName(): String = SHORT_MONTH_NAMES[ordinal]
