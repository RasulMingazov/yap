package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.persistence.TopicRepository

class GetTopicUseCase(
    private val topicDetailsRule: TopicDetailsRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(topicId: TopicId): TopicDetails? =
        topicRepository.find(topicId)?.let { topic -> topicDetailsRule.details(topic) }
}
