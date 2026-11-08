package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicStatus
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicWrite
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
    fun `GIVEN a date with a published topic WHEN updating THEN the date is taken`() = runTest {
        val env = Environment()
        env.topicRepository.updateCall.returns(TopicWrite.DateTaken)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = UpdateTopicOutcome.DateTaken, actual = outcome)
    }

    @Test
    fun `GIVEN a topic published after it was read WHEN updating THEN the status is stale`() = runTest {
        val env = Environment()
        env.topicRepository.updateCall.returns(TopicWrite.StaleStatus)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        assertEquals(expected = UpdateTopicOutcome.StaleStatus, actual = outcome)
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
        assertEquals(expected = UpdateTopicOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.findCall.returns(null)

        val outcome = env.useCase(content = StubTopic.stubContent(), topicId = StubTopic.TOPIC_ID)

        env.topicRepository.updateCall.notCalled()
        assertEquals(expected = UpdateTopicOutcome.NotFound, actual = outcome)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val useCase = UpdateTopicUseCase(
            topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            ),
            topicRepository = topicRepository,
        )
    }
}
