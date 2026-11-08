package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.persistence.StubTopicRepository
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class TopicDetailsRuleTest {

    @Test
    fun `GIVEN a draft with every field and a free date WHEN reading the details THEN it names nothing`() = runTest {
        val env = Environment()
        val draft = StubTopic.stubTopic()

        val details = env.rule.details(draft)

        assertEquals(
            expected = TopicDetails(gaps = emptySet(), state = ReleaseState.DRAFT, topic = draft),
            actual = details,
        )
    }

    @Test
    fun `GIVEN a draft on a date with a published topic WHEN reading the details THEN it names the taken date`() =
        runTest {
            val env = Environment()
            env.topicRepository.hasPublishedOnCall.returns(true)

            val details = env.rule.details(StubTopic.stubTopic())

            env.topicRepository.hasPublishedOnCall.calledWith(StubTopic.RELEASE_DATE, StubTopic.TOPIC_ID)
            assertEquals(expected = setOf(TopicGap.DateTaken), actual = details.gaps)
        }

    @Test
    fun `GIVEN a published topic that is out WHEN reading the details THEN it keeps its past date`() = runTest {
        val env = Environment()
        val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))
        val published = StubTopic.stubTopic(content = content, status = TopicStatus.PUBLISHED)

        val details = env.rule.details(published)

        env.topicRepository.hasPublishedOnCall.notCalled()
        assertEquals(
            expected = TopicDetails(gaps = emptySet(), state = ReleaseState.OUT, topic = published),
            actual = details,
        )
    }

    private class Environment {

        val topicRepository = StubTopicRepository()
        val rule = TopicDetailsRule(
            clock = StubTopic.CLOCK,
            topicPublishRule = TopicPublishRule(
                topicDraftRule = TopicDraftRule(
                    getCharacterUseCase = GetCharacterUseCase(characterRepository = StubCharacterRepository()),
                ),
            ),
            topicRepository = topicRepository,
        )
    }
}
