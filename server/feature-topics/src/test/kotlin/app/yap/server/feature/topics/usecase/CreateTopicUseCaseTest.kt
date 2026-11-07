package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class CreateTopicUseCaseTest {

    @Test
    fun `GIVEN a draft that is ready WHEN creating THEN the topic is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubTopic.stubContent())

        env.topicRepository.createCall.calledWith(StubTopic.stubContent())
        assertEquals(expected = CreateTopicOutcome.Created(StubTopic.stubTopic()), actual = outcome)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN creating THEN nothing is stored`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val outcome = env.useCase(StubTopic.stubContent())

        env.topicRepository.createCall.notCalled()
        assertEquals(expected = CreateTopicOutcome.Invalid, actual = outcome)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val useCase = CreateTopicUseCase(
            topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            ),
            topicRepository = topicRepository,
        )
    }
}
