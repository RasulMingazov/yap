package app.yap.server.feature.topics.model

data class Topic(
    val content: TopicContent,
    val status: TopicStatus,
    val topicId: TopicId,
)
