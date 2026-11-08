package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.TopicRepository

class CreateTopicUseCase(
    private val topicDetailsRule: TopicDetailsRule,
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(content: TopicContent): CreateTopicOutcome {
        val gaps = topicDraftRule.gaps(content)
        return if (gaps.isEmpty()) {
            CreateTopicOutcome.Created(topicDetailsRule.details(topicRepository.create(content)))
        } else {
            CreateTopicOutcome.Invalid(gaps)
        }
    }
}
