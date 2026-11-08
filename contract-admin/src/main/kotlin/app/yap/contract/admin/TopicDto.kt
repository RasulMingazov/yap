package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class TopicDto(
    val content: TopicContentDto,
    val gaps: List<TopicGapDto>,
    val status: TopicStatusDto,
    val topicId: String,
)
