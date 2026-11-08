package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

internal class GetTopicUseCaseTest {

    @Test
    fun `GIVEN a stored draft WHEN reading it THEN the topic comes with its details`() = runTest {
        val env = Environment()

        val details = env.useCase(StubTopic.TOPIC_ID)

        assertEquals(
            expected = TopicDetails(gaps = emptySet(), state = ReleaseState.DRAFT, topic = StubTopic.stubTopic()),
            actual = details,
        )
    }

    @Test
    fun `GIVEN a stored draft on a date with a published topic WHEN reading it THEN the details name the taken date`() =
        runTest {
            val env = Environment()
            env.topicRepository.hasPublishedOnCall.returns(true)

            val details = env.useCase(StubTopic.TOPIC_ID)

            env.topicRepository.hasPublishedOnCall.calledWith(StubTopic.RELEASE_DATE, StubTopic.TOPIC_ID)
            assertEquals(expected = setOf(TopicGap.DateTaken), actual = details?.gaps)
        }

    @Test
    fun `GIVEN an unknown topic WHEN reading it THEN there are no details`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(null)

        assertNull(env.useCase(StubTopic.TOPIC_ID))
    }

    private class Environment {

        val topicRepository = StubTopicRepository()
        val useCase = GetTopicUseCase(
            clock = StubTopic.CLOCK,
            topicDetailsRule = TopicDetailsRule(
                topicPublishRule = TopicPublishRule(
                    topicDraftRule = TopicDraftRule(
                        getCharacterUseCase = GetCharacterUseCase(characterRepository = StubCharacterRepository()),
                    ),
                ),
            ),
            topicRepository = topicRepository,
        )
    }
}
