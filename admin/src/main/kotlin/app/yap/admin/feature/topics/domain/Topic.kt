package app.yap.admin.feature.topics.domain

internal data class Topic(
    val content: TopicContent,
    val gaps: List<TopicGap>,
    val state: ReleaseState,
    val topicId: TopicId,
)
