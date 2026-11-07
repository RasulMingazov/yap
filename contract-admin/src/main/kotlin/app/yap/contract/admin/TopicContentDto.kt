package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class TopicContentDto(
    val description: String,
    val durationMinutes: Int?,
    val interlocutor: InterlocutorDto?,
    val openingLine: OpeningLineDto,
    val releaseDate: String?,
    val role: String,
    val setting: String,
    val steps: List<StepDto>,
    val title: String,
)
