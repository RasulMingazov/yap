package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicField
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.model.TopicStatus
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicRepository
import app.yap.server.feature.topics.persistence.TopicWrite
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class UpdateTopicUseCaseTest {

    @Test
    fun `GIVEN a draft that is ready WHEN updating THEN the topic is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.calledWith(StubTopic.stubTopic())
        assertEquals(expected = UpdateTopicOutcome.Updated(StubTopic.stubTopic()), actual = outcome)
    }

    @Test
    fun `GIVEN a draft with an empty setting WHEN updating THEN the draft is stored as a draft`() = runTest {
        val env = Environment()
        val content = StubTopic.stubContent(setting = "")

        env.useCase(content = content, topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.calledWith(StubTopic.stubTopic(content = content))
    }

    @Test
    fun `GIVEN a published topic WHEN updating THEN the topic is stored with the status it read`() = runTest {
        val env = Environment()
        val published = StubTopic.stubTopic(status = TopicStatus.PUBLISHED)
        env.topicRepository.findCall.returns(published)

        env.useCase(content = StubTopic.stubContent(title = "Changed"), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.calledWith(
            published.copy(content = StubTopic.stubContent(title = "Changed")),
        )
    }

    @Test
    fun `GIVEN a published topic WHEN updating it with an empty setting THEN nothing is stored`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(StubTopic.stubTopic(status = TopicStatus.PUBLISHED))

        val outcome = env.useCase(content = StubTopic.stubContent(setting = ""), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.notCalled()
        assertEquals(
            expected = UpdateTopicOutcome.Invalid(setOf(TopicGap.Missing(TopicField.SETTING))),
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a published topic WHEN moving it into the past THEN nothing is stored`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(StubTopic.stubTopic(status = TopicStatus.PUBLISHED))
        val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))

        val outcome = env.useCase(content = content, topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.notCalled()
        assertEquals(expected = UpdateTopicOutcome.Invalid(setOf(TopicGap.PastDate)), actual = outcome)
    }

    @Test
    fun `GIVEN a date with a published topic WHEN updating THEN the date is taken`() = runTest {
        val env = Environment()
        env.topicRepository.updateCall.returns(TopicWrite.DateTaken)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = UpdateTopicOutcome.DateTaken, actual = outcome)
    }

    @Test
    fun `GIVEN a topic published after it was read WHEN updating THEN the publish rule runs on the new status`() =
        runTest {
            val env = Environment { stub -> PublishedAfterFirstUpdate(stub) }

            val outcome = env.useCase(content = StubTopic.stubContent(setting = ""), topicId = StubTopic.TOPIC_ID)

            env.topicRepository.updateCall.notCalled()
            assertEquals(
                expected = UpdateTopicOutcome.Invalid(setOf(TopicGap.Missing(TopicField.SETTING))),
                actual = outcome,
            )
        }

    @Test
    fun `GIVEN a topic that is gone before the write WHEN updating THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.updateCall.returns(TopicWrite.NotFound)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = UpdateTopicOutcome.NotFound, actual = outcome)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN updating THEN nothing is stored`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.notCalled()
        assertEquals(
            expected = UpdateTopicOutcome.Invalid(setOf(TopicGap.Missing(TopicField.INTERLOCUTOR))),
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(null)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.notCalled()
        assertEquals(expected = UpdateTopicOutcome.NotFound, actual = outcome)
    }

    private class PublishedAfterFirstUpdate(
        private val stub: StubTopicRepository,
    ) : TopicRepository by stub {

        private var isPublished = false

        override suspend fun find(topicId: TopicId): Topic? =
            stub.find(topicId)?.copy(status = if (isPublished) TopicStatus.PUBLISHED else TopicStatus.DRAFT)

        override suspend fun update(topic: Topic): TopicWrite =
            if (isPublished) {
                stub.update(topic)
            } else {
                isPublished = true
                TopicWrite.StaleStatus
            }
    }

    private class Environment(wrap: (StubTopicRepository) -> TopicRepository = { stub -> stub }) {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val topicDraftRule = TopicDraftRule(
            getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
        )
        val useCase = UpdateTopicUseCase(
            clock = StubTopic.CLOCK,
            topicDraftRule = topicDraftRule,
            topicPublishRule = TopicPublishRule(topicDraftRule = topicDraftRule),
            topicRepository = wrap(topicRepository),
        )
    }
}
