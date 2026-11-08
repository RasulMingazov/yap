package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.model.TopicStatus
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.TopicRepository
import app.yap.server.feature.topics.persistence.TopicWrite
import java.time.Clock

class UpdateTopicUseCase(
    private val clock: Clock,
    private val topicDraftRule: TopicDraftRule,
    private val topicPublishRule: TopicPublishRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(
        content: TopicContent,
        topicId: TopicId,
    ): UpdateTopicOutcome {
        val stored = topicRepository.find(topicId)
        return if (stored == null) UpdateTopicOutcome.NotFound else update(content, stored)
    }

    private suspend fun update(content: TopicContent, stored: Topic): UpdateTopicOutcome {
        val gaps = gaps(content, stored)
        return if (gaps.isEmpty()) {
            val write = topicRepository.update(stored.copy(content = content))
            write.toOutcome(content, stored.topicId)
        } else {
            UpdateTopicOutcome.Invalid(gaps)
        }
    }

    private suspend fun gaps(content: TopicContent, stored: Topic): Set<TopicGap> = when (stored.status) {
        TopicStatus.DRAFT -> topicDraftRule.gaps(content)
        TopicStatus.PUBLISHED -> topicPublishRule.gaps(
            content = content,
            publishedDate = stored.publishedDate,
            today = ReleaseState.today(clock),
        )
    }

    private suspend fun TopicWrite.toOutcome(content: TopicContent, topicId: TopicId): UpdateTopicOutcome = when (this) {
        is TopicWrite.DateTaken -> UpdateTopicOutcome.DateTaken
        is TopicWrite.NotFound -> UpdateTopicOutcome.NotFound
        is TopicWrite.StaleStatus -> invoke(content, topicId)
        is TopicWrite.Written -> UpdateTopicOutcome.Updated(topic)
    }
}
