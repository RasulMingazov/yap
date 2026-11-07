package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.persistence.TopicRepository

class GetTopicsUseCase(
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(): List<Topic> = topicRepository.list()
}
