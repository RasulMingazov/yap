package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.TopicRepository

class UpdateTopicUseCase(
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(topic: Topic): UpdateTopicOutcome {
        if (topicDraftRule.gaps(topic.content).isNotEmpty()) return UpdateTopicOutcome.Invalid
        return if (topicRepository.update(topic)) {
            UpdateTopicOutcome.Updated(topic)
        } else {
            UpdateTopicOutcome.NotFound
        }
    }
}
