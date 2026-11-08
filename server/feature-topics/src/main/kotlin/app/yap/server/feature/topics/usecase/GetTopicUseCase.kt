package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class GetTopicUseCase(
    private val clock: Clock,
    private val topicDetailsRule: TopicDetailsRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(topicId: TopicId): TopicDetails? {
        val topic = topicRepository.find(topicId) ?: return null
        return topicDetailsRule.details(
            isDateTaken = topicRepository.hasPublishedOn(topic.date, topic.topicId),
            today = ReleaseState.today(clock),
            topic = topic,
        )
    }
}
