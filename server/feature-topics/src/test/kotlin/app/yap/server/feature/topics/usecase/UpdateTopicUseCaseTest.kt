package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class UpdateTopicUseCaseTest {

    @Test
    fun `GIVEN a draft that is ready WHEN updating THEN the topic is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubTopic.stubTopic())

        env.topicRepository.updateCall.calledWith(StubTopic.stubTopic())
        assertEquals(expected = UpdateTopicOutcome.Updated(StubTopic.stubTopic()), actual = outcome)
    }

    @Test
    fun `GIVEN a draft that is not ready WHEN updating THEN nothing is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubTopic.stubTopic(content = StubTopic.stubContent(releaseDate = null)))

        env.topicRepository.updateCall.notCalled()
        assertEquals(expected = UpdateTopicOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN updating THEN nothing is stored`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val outcome = env.useCase(StubTopic.stubTopic())

        env.topicRepository.updateCall.notCalled()
        assertEquals(expected = UpdateTopicOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN a guest and no characters WHEN updating THEN the topic is stored`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)
        val topic = StubTopic.stubTopic(
            content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist.")),
        )

        val outcome = env.useCase(topic)

        env.topicRepository.updateCall.calledWith(topic)
        assertEquals(expected = UpdateTopicOutcome.Updated(topic), actual = outcome)
    }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN it is not found`() = runTest {
        val env = Environment()
        env.topicRepository.updateCall.returns(false)

        val outcome = env.useCase(StubTopic.stubTopic())

        assertEquals(expected = UpdateTopicOutcome.NotFound, actual = outcome)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val useCase = UpdateTopicUseCase(
            getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            topicRepository = topicRepository,
        )
    }
}
