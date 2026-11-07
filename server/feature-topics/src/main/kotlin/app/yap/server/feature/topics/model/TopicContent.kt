package app.yap.server.feature.topics.model

import java.time.LocalDate

data class TopicContent(
    val description: String,
    val durationMinutes: Int?,
    val interlocutor: Interlocutor?,
    val openingLine: OpeningLine,
    val releaseDate: LocalDate?,
    val role: String,
    val setting: String,
    val steps: List<Step>,
    val title: String,
)

internal fun TopicContent.trimmedIfDraft(): TopicContent? {
    val trimmed = copy(
        description = description.trim(),
        interlocutor = interlocutor?.trimmed(),
        openingLine = openingLine.trimmed(),
        role = role.trim(),
        setting = setting.trim(),
        steps = steps.map(Step::trimmed).filterNot(Step::isEmpty),
        title = title.trim(),
    )
    val isDraft = trimmed.title.isNotEmpty() &&
        trimmed.releaseDate != null &&
        (trimmed.durationMinutes ?: 1) >= 1
    return trimmed.takeIf { isDraft }
}

private fun Interlocutor.trimmed(): Interlocutor = when (this) {
    is Interlocutor.Cast -> this
    is Interlocutor.Guest -> Interlocutor.Guest(description = description.trim())
}

private fun OpeningLine.trimmed(): OpeningLine = OpeningLine(
    english = english.trim(),
    translation = translation.trim(),
)

private fun Step.trimmed(): Step = Step(
    outcome = outcome.trim(),
    passWhen = passWhen.trim(),
    task = task.trim(),
)

private fun Step.isEmpty(): Boolean = listOf(outcome, passWhen, task).all(String::isEmpty)
