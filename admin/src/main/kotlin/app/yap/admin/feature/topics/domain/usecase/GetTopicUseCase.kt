package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository

internal interface GetTopicUseCase {

    suspend operator fun invoke(topicId: TopicId): LoadTopicResult
}

internal class DefaultGetTopicUseCase(
    private val topicRepository: TopicRepository,
) : GetTopicUseCase {

    override suspend fun invoke(topicId: TopicId): LoadTopicResult = topicRepository.find(topicId)
}
