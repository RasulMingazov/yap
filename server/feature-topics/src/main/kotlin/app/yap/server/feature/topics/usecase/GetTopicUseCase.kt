package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.persistence.TopicRepository

class GetTopicUseCase(
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(topicId: TopicId): Topic? = topicRepository.find(topicId)
}
