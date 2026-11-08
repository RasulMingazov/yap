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
        addAll(missingFields(content))
        if (isPastDate(content.releaseDate, publishedDate, today)) add(TopicGap.PastDate)
        val numbers = incompleteStepNumbers(content.steps)
        if (numbers.isNotEmpty()) add(TopicGap.IncompleteSteps(numbers))
    }

    private fun missingFields(content: TopicContent): List<TopicGap.Missing> = listOf(
        TopicField.DURATION to (content.durationMinutes == null),
        TopicField.DESCRIPTION to content.description.isEmpty(),
        TopicField.SETTING to content.setting.isEmpty(),
        TopicField.INTERLOCUTOR to isMissing(content.interlocutor),
        TopicField.ROLE to content.role.isEmpty(),
        TopicField.OPENING_LINE to content.openingLine.english.isEmpty(),
        TopicField.TRANSLATION to content.openingLine.translation.isEmpty(),
        TopicField.STEPS to content.steps.isEmpty(),
    )
        .filter { (_, isEmpty) -> isEmpty }
        .map { (field, _) -> TopicGap.Missing(field) }

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
