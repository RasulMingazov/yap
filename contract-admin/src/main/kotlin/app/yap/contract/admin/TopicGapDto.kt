package app.yap.contract.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TopicGapDto {

    @Serializable
    @SerialName("dateTaken")
    data object DateTakenDto : TopicGapDto

    @Serializable
    @SerialName("incompleteSteps")
    data class IncompleteStepsDto(val numbers: List<Int>) : TopicGapDto

    @Serializable
    @SerialName("missing")
    data class MissingDto(val field: TopicFieldDto) : TopicGapDto

    @Serializable
    @SerialName("pastDate")
    data object PastDateDto : TopicGapDto
}
