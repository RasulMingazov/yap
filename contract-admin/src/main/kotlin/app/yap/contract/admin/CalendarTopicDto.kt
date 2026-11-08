package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class CalendarTopicDto(
    val completeSteps: Int,
    val ledBy: LedByDto?,
    val status: TopicStatusDto,
    val steps: Int,
    val title: String,
    val topicId: String,
)
