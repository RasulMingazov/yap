package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository

internal interface SaveTopicUseCase {

    suspend operator fun invoke(topicId: TopicId?, content: TopicContent): SaveTopicResult
}

internal class DefaultSaveTopicUseCase(
    private val topicRepository: TopicRepository,
) : SaveTopicUseCase {

    override suspend fun invoke(topicId: TopicId?, content: TopicContent): SaveTopicResult {
        if (topicId == null) return topicRepository.create(content)
        return topicRepository.update(
            content = content,
            topicId = topicId,
        )
    }
}
