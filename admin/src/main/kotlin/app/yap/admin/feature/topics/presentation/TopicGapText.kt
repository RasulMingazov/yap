package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.TopicField
import app.yap.admin.feature.topics.domain.TopicGap

internal object TopicGapText {

    operator fun invoke(gaps: List<TopicGap>): String {
        val phrases = gaps.map { gap -> gap.phrase() }
        val head = phrases.dropLast(1)
        if (head.isEmpty()) return phrases.joinToString()
        return "${head.joinToString(separator = ", ")} и ${phrases.last()}"
    }

    private fun TopicGap.phrase(): String = when (this) {
        is TopicGap.DateTaken -> "другую дату выхода"
        is TopicGap.IncompleteSteps -> steps(numbers)
        is TopicGap.Missing -> field.phrase()
        is TopicGap.PastDate -> "новую дату выхода"
    }

    private fun steps(numbers: List<Int>): String {
        val noun = if (numbers.size == 1) "шаг" else "шаги"
        val runs = runs(numbers).joinToString(separator = ", ") { run -> run.text() }
        return "$noun $runs"
    }

    private fun runs(numbers: List<Int>): List<IntRange> = numbers.fold(emptyList()) { runs, number ->
        val last = runs.lastOrNull()
        if (last != null && last.last + 1 == number) {
            runs.dropLast(1) + listOf(last.first..number)
        } else {
            runs + listOf(number..number)
        }
    }

    private fun IntRange.text(): String = if (first == last) "$first" else "$first–$last"

    private fun TopicField.phrase(): String = when (this) {
        TopicField.TITLE -> "название"
        TopicField.RELEASE_DATE -> "дату выхода"
        TopicField.DURATION -> "длительность"
        TopicField.DESCRIPTION -> "описание"
        TopicField.SETTING -> "обстановку"
        TopicField.INTERLOCUTOR -> "собеседника"
        TopicField.ROLE -> "роль"
        TopicField.OPENING_LINE -> "первую реплику"
        TopicField.TRANSLATION -> "перевод"
        TopicField.STEPS -> "шаги"
    }
}
