package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.PublishTopicResult
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository

internal interface PublishTopicUseCase {

    suspend operator fun invoke(topicId: TopicId, content: TopicContent): PublishTopicResult
}

internal class DefaultPublishTopicUseCase(
    private val topicRepository: TopicRepository,
) : PublishTopicUseCase {

    override suspend fun invoke(topicId: TopicId, content: TopicContent): PublishTopicResult =
        topicRepository.publish(
            content = content,
            topicId = topicId,
        )
}
