package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.PublishTopicOutcome
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.TopicPublish
import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class PublishTopicUseCase(
    private val clock: Clock,
    private val topicDetailsRule: TopicDetailsRule,
    private val topicPublishRule: TopicPublishRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(
        content: TopicContent,
        topicId: TopicId,
    ): PublishTopicOutcome {
        val stored = topicRepository.find(topicId)
        return if (stored == null) PublishTopicOutcome.NotFound else publish(content, stored)
    }

    private suspend fun publish(content: TopicContent, stored: Topic): PublishTopicOutcome {
        val gaps = topicPublishRule.gaps(
            content = content,
            publishedDate = stored.publishedDate,
            today = ReleaseState.today(clock),
        )
        return if (gaps.isEmpty()) {
            val write = topicRepository.publish(content, stored.topicId)
            write.toOutcome()
        } else {
            PublishTopicOutcome.Invalid(gaps)
        }
    }

    private suspend fun TopicPublish.toOutcome(): PublishTopicOutcome = when (this) {
        is TopicPublish.DateTaken -> PublishTopicOutcome.DateTaken
        is TopicPublish.NotFound -> PublishTopicOutcome.NotFound
        is TopicPublish.Published -> PublishTopicOutcome.Published(details(topic))
    }

    private suspend fun details(topic: Topic): TopicDetails = topicDetailsRule.details(
        isDateTaken = topicRepository.hasPublishedOn(topic.date, topic.topicId),
        today = ReleaseState.today(clock),
        topic = topic,
    )
}
