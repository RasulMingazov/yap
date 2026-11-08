package app.yap.server.feature.topics.model

import java.time.LocalDate

data class Topic(
    val content: TopicContent,
    val status: TopicStatus,
    val topicId: TopicId,
) {

    internal val date: LocalDate
        get() = requireNotNull(content.releaseDate) { "A stored topic has a release date" }

    internal val publishedDate: LocalDate?
        get() = date.takeIf { status == TopicStatus.PUBLISHED }
}
