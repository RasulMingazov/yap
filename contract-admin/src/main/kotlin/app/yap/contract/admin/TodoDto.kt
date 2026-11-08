package app.yap.contract.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TodoDto {

    @Serializable
    @SerialName("draft")
    data class DraftDto(
        val date: String,
        val gaps: List<TopicGapDto>,
        val title: String,
        val topicId: String,
    ) : TodoDto

    @Serializable
    @SerialName("emptyDay")
    data class EmptyDayDto(val date: String) : TodoDto
}
