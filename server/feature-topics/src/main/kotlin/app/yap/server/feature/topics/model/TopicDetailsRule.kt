package app.yap.server.feature.topics.model

import java.time.LocalDate

class TopicDetailsRule(
    private val topicPublishRule: TopicPublishRule,
) {

    suspend fun details(
        isDateTaken: Boolean,
        today: LocalDate,
        topic: Topic,
    ): TopicDetails {
        val gaps = topicPublishRule.gaps(topic.content, publishedDate = topic.publishedDate, today = today)
        val isTaken = isDateTaken && topic.status == TopicStatus.DRAFT
        return TopicDetails(
            gaps = if (isTaken) gaps + TopicGap.DateTaken else gaps,
            state = ReleaseState.of(topic.status, topic.date, today),
            topic = topic,
        )
    }
}
