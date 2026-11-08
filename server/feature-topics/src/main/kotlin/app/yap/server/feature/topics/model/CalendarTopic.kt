package app.yap.server.feature.topics.model

data class CalendarTopic(
    val completeSteps: Int,
    val ledBy: LedBy?,
    val state: ReleaseState,
    val steps: Int,
    val title: String,
    val topicId: TopicId,
)
