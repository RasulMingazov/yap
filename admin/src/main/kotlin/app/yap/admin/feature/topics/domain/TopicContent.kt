package app.yap.admin.feature.topics.domain

import java.time.LocalDate

internal data class TopicContent(
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
