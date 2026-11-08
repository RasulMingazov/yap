package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.moscowToday
import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class GetTopicsUseCase(
    private val clock: Clock,
    private val topicDetailsRule: TopicDetailsRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(): List<TopicDetails> {
        val today = moscowToday(clock)
        return topicRepository.list().map { topic ->
            topicDetailsRule.details(
                isDateTaken = topicRepository.hasPublishedOn(topic.date, topic.topicId),
                today = today,
                topic = topic,
            )
        }
    }
}
