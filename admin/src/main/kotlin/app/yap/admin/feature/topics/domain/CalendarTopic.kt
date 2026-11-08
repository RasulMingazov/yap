package app.yap.admin.feature.topics.domain

internal data class CalendarTopic(
    val completeSteps: Int,
    val ledBy: LedBy?,
    val state: ReleaseState,
    val steps: Int,
    val title: String,
    val topicId: TopicId,
)
