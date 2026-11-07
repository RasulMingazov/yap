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

    val isDraftReady: Boolean
        get() = title.isNotEmpty() && releaseDate != null && (durationMinutes ?: 1) >= 1
}
