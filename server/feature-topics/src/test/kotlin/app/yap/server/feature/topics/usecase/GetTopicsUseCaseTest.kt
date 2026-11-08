package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class GetTopicsUseCaseTest {

    @Test
    fun `GIVEN a draft on a date with a published topic WHEN listing the topics THEN the details name the taken date`() =
        runTest {
            val topicRepository = StubTopicRepository()
            topicRepository.hasPublishedOnCall.returns(true)
            val useCase = GetTopicsUseCase(
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

            val details = useCase()

            topicRepository.hasPublishedOnCall.calledWith(StubTopic.RELEASE_DATE, StubTopic.TOPIC_ID)
            assertEquals(
                expected = listOf(StubTopic.stubDetails(gaps = setOf(TopicGap.DateTaken))),
                actual = details,
            )
        }
}
