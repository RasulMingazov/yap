package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class CreateTopicUseCase(
    private val clock: Clock,
    private val topicDetailsRule: TopicDetailsRule,
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(content: TopicContent): CreateTopicOutcome {
        val gaps = topicDraftRule.gaps(content)
        return if (gaps.isEmpty()) {
            val topic = topicRepository.create(content)
            val details = topicDetailsRule.details(
                isDateTaken = topicRepository.hasPublishedOn(topic.date, topic.topicId),
                today = ReleaseState.today(clock),
                topic = topic,
            )
            CreateTopicOutcome.Created(details)
        } else {
            CreateTopicOutcome.Invalid(gaps)
        }
    }
}
