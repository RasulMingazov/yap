package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicField
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class CreateTopicUseCaseTest {

    @Test
    fun `GIVEN a draft that is ready WHEN creating THEN the topic is stored with its details`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubTopic.stubContent())

        env.topicRepository.createCall.calledWith(StubTopic.stubContent())
        assertEquals(
            expected = CreateTopicOutcome.Created(
                TopicDetails(gaps = emptySet(), state = ReleaseState.DRAFT, topic = StubTopic.stubTopic()),
            ),
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a draft on a date with a published topic WHEN creating THEN the details name the taken date`() =
        runTest {
            val env = Environment()
            env.topicRepository.hasPublishedOnCall.returns(true)

            val outcome = env.useCase(StubTopic.stubContent())

            env.topicRepository.hasPublishedOnCall.calledWith(StubTopic.RELEASE_DATE, StubTopic.TOPIC_ID)
            assertEquals(
                expected = CreateTopicOutcome.Created(
                    TopicDetails(
                        gaps = setOf(TopicGap.DateTaken),
                        state = ReleaseState.DRAFT,
                        topic = StubTopic.stubTopic(),
                    ),
                ),
                actual = outcome,
            )
        }

    @Test
    fun `GIVEN a character that does not exist WHEN creating THEN nothing is stored`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val outcome = env.useCase(StubTopic.stubContent())

        env.topicRepository.createCall.notCalled()
        assertEquals(
            expected = CreateTopicOutcome.Invalid(setOf(TopicGap.Missing(TopicField.INTERLOCUTOR))),
            actual = outcome,
        )
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val topicDraftRule = TopicDraftRule(
            getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
        )
        val useCase = CreateTopicUseCase(
            clock = StubTopic.CLOCK,
            topicDetailsRule = TopicDetailsRule(
                topicPublishRule = TopicPublishRule(topicDraftRule = topicDraftRule),
            ),
            topicDraftRule = topicDraftRule,
            topicRepository = topicRepository,
        )
    }
}
