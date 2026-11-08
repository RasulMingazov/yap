package app.yap.server.feature.topics.model

import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class TopicDetailsRule(
    private val clock: Clock,
    private val topicPublishRule: TopicPublishRule,
    private val topicRepository: TopicRepository,
) {

    suspend fun details(topic: Topic): TopicDetails {
        val today = ReleaseState.today(clock)
        val gaps = topicPublishRule.gaps(topic.content, publishedDate = topic.publishedDate, today = today)
        val isDateTaken = topic.status == TopicStatus.DRAFT &&
            topicRepository.hasPublishedOn(topic.date, topic.topicId)
        return TopicDetails(
            gaps = if (isDateTaken) gaps + TopicGap.DateTaken else gaps,
            state = ReleaseState.of(topic.status, topic.date, today),
            topic = topic,
        )
    }
}
