package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.TopicRepository

class CreateTopicUseCase(
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(content: TopicContent): CreateTopicOutcome {
        if (!topicDraftRule.accepts(content)) return CreateTopicOutcome.Invalid
        return CreateTopicOutcome.Created(topicRepository.create(content))
    }
}
