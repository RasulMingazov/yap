package app.yap.server.feature.topics.model

import java.time.LocalDate

class TopicPublishRule(
    private val topicDraftRule: TopicDraftRule,
) {

    suspend fun gaps(
        content: TopicContent,
        publishedDate: LocalDate?,
        today: LocalDate,
    ): Set<TopicGap> = buildSet {
        addAll(topicDraftRule.gaps(content))
        addAll(missingFields(content).map { field -> TopicGap.Missing(field) })
        if (isPastDate(content.releaseDate, publishedDate, today)) add(TopicGap.PastDate)
        val numbers = incompleteStepNumbers(content.steps)
        if (numbers.isNotEmpty()) add(TopicGap.IncompleteSteps(numbers))
    }

    private fun missingFields(content: TopicContent): List<TopicField> = listOfNotNull(
        TopicField.DURATION.takeIf { content.durationMinutes == null },
        TopicField.DESCRIPTION.takeIf { content.description.isEmpty() },
        TopicField.SETTING.takeIf { content.setting.isEmpty() },
        TopicField.INTERLOCUTOR.takeIf { isMissing(content.interlocutor) },
        TopicField.ROLE.takeIf { content.role.isEmpty() },
        TopicField.OPENING_LINE.takeIf { content.openingLine.english.isEmpty() },
        TopicField.TRANSLATION.takeIf { content.openingLine.translation.isEmpty() },
        TopicField.STEPS.takeIf { content.steps.isEmpty() },
    )

    private fun isMissing(interlocutor: Interlocutor?): Boolean = when (interlocutor) {
        null -> true
        is Interlocutor.Cast -> false
        is Interlocutor.Guest -> interlocutor.description.isEmpty()
    }

    private fun isPastDate(
        releaseDate: LocalDate?,
        publishedDate: LocalDate?,
        today: LocalDate,
    ): Boolean = releaseDate != null && releaseDate.isBefore(today) && releaseDate != publishedDate

    private fun incompleteStepNumbers(steps: List<Step>): List<Int> = steps
        .mapIndexedNotNull { index, step -> (index + 1).takeUnless { step.isComplete } }
}
