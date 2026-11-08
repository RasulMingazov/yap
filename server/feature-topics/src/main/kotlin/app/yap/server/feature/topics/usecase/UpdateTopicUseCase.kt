package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.TopicRepository

class UpdateTopicUseCase(
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(
        content: TopicContent,
        topicId: TopicId,
    ): UpdateTopicOutcome {
        if (topicDraftRule.gaps(content).isNotEmpty()) return UpdateTopicOutcome.Invalid
        val stored = topicRepository.find(topicId)
        return if (stored == null) {
            UpdateTopicOutcome.NotFound
        } else {
            topicRepository.update(stored.copy(content = content))
        }
    }
}
