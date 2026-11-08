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
) {

    internal fun normalized(): TopicContent = copy(
        description = description.trim(),
        interlocutor = interlocutor?.normalized(),
        openingLine = openingLine.normalized(),
        role = role.trim(),
        setting = setting.trim(),
        steps = steps
            .map { step -> step.normalized() }
            .filterNot { step -> step.isEmpty },
        title = title.trim(),
    )
}

private fun Interlocutor.normalized(): Interlocutor = when (this) {
    is Interlocutor.Cast -> this
    is Interlocutor.Guest -> copy(description = description.trim())
}
