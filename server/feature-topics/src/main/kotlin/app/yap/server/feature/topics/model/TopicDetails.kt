package app.yap.server.feature.topics.model

data class TopicDetails(
    val gaps: Set<TopicGap>,
    val state: ReleaseState,
    val topic: Topic,
)
