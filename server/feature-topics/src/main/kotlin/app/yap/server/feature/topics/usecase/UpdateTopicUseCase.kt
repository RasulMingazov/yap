package app.yap.server.feature.topics.usecase

import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.TopicRepository
import app.yap.server.feature.topics.persistence.TopicWrite

class UpdateTopicUseCase(
    private val topicDraftRule: TopicDraftRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(
        content: TopicContent,
        topicId: TopicId,
    ): UpdateTopicOutcome {
        val gaps = topicDraftRule.gaps(content)
        if (gaps.isNotEmpty()) return UpdateTopicOutcome.Invalid
        val stored = topicRepository.find(topicId)
        return if (stored == null) {
            UpdateTopicOutcome.NotFound
        } else {
            topicRepository.update(stored.copy(content = content)).toOutcome()
        }
    }
}

private fun TopicWrite.toOutcome(): UpdateTopicOutcome = when (this) {
    is TopicWrite.DateTaken -> UpdateTopicOutcome.DateTaken
    is TopicWrite.NotFound -> UpdateTopicOutcome.NotFound
    is TopicWrite.StaleStatus -> UpdateTopicOutcome.StaleStatus
    is TopicWrite.Written -> UpdateTopicOutcome.Updated(topic)
}
