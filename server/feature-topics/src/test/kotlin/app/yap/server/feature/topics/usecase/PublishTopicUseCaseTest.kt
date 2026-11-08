package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.PublishTopicOutcome
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicField
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.model.TopicStatus
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicPublish
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class PublishTopicUseCaseTest {

    @Test
    fun `GIVEN a draft with every field WHEN publishing THEN the topic is published with its details`() = runTest {
        val env = Environment()
        val published = StubTopic.stubTopic(status = TopicStatus.PUBLISHED)
        env.topicRepository.publishCall.returns(TopicPublish.Published(published))

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.publishCall.calledWith(StubTopic.stubContent(), StubTopic.TOPIC_ID)
        assertEquals(
            expected = PublishTopicOutcome.Published(
                TopicDetails(gaps = emptySet(), state = ReleaseState.PUBLISHED, topic = published),
            ),
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a draft without a setting WHEN publishing THEN nothing is published`() = runTest {
        val env = Environment()

        val outcome = env.useCase(content = StubTopic.stubContent(setting = ""), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.publishCall.notCalled()
        assertEquals(
            expected = PublishTopicOutcome.Invalid(setOf(TopicGap.Missing(TopicField.SETTING))),
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a draft with a past date WHEN publishing THEN nothing is published`() = runTest {
        val env = Environment()
        val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))
        env.topicRepository.findCall.returns(StubTopic.stubTopic(content = content))

        val outcome = env.useCase(content = content, topicId = StubTopic.TOPIC_ID)

        env.topicRepository.publishCall.notCalled()
        assertEquals(expected = PublishTopicOutcome.Invalid(setOf(TopicGap.PastDate)), actual = outcome)
    }

    @Test
    fun `GIVEN a published topic that is out WHEN publishing it with the same date THEN it acts as a save`() =
        runTest {
            val env = Environment()
            val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))
            val out = StubTopic.stubTopic(content = content, status = TopicStatus.PUBLISHED)
            env.topicRepository.findCall.returns(out)
            env.topicRepository.publishCall.returns(TopicPublish.Published(out))

            val outcome = env.useCase(content = content, topicId = StubTopic.TOPIC_ID)

            env.topicRepository.publishCall.calledWith(content, StubTopic.TOPIC_ID)
            assertEquals(
                expected = PublishTopicOutcome.Published(
                    TopicDetails(gaps = emptySet(), state = ReleaseState.OUT, topic = out),
                ),
                actual = outcome,
            )
        }

    @Test
    fun `GIVEN an unknown topic WHEN publishing it THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(null)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.publishCall.notCalled()
        assertEquals(expected = PublishTopicOutcome.NotFound, actual = outcome)
    }

    @Test
    fun `GIVEN a topic that is gone before the write WHEN publishing THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.publishCall.returns(TopicPublish.NotFound)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = PublishTopicOutcome.NotFound, actual = outcome)
    }

    @Test
    fun `GIVEN a date with a published topic WHEN publishing THEN the date is taken`() = runTest {
        val env = Environment()
        env.topicRepository.publishCall.returns(TopicPublish.DateTaken)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = PublishTopicOutcome.DateTaken, actual = outcome)
    }

    private class Environment {

        val topicRepository = StubTopicRepository()
        val topicPublishRule = TopicPublishRule(
            topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = StubCharacterRepository()),
            ),
        )
        val useCase = PublishTopicUseCase(
            clock = StubTopic.CLOCK,
            topicDetailsRule = TopicDetailsRule(topicPublishRule = topicPublishRule),
            topicPublishRule = topicPublishRule,
            topicRepository = topicRepository,
        )
    }
}
