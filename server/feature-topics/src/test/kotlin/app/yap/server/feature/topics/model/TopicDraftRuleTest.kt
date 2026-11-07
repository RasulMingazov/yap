package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

internal class TopicDraftRuleTest {

    @Test
    fun `GIVEN a draft that is ready and a character that exists WHEN checking THEN the draft is accepted`() =
        runTest {
            val env = Environment()

            val accepts = env.rule.accepts(StubTopic.stubContent())

            assertTrue(accepts)
        }

    @Test
    fun `GIVEN a draft that is not ready WHEN checking THEN the draft is rejected`() = runTest {
        val env = Environment()

        val accepts = env.rule.accepts(StubTopic.stubContent(title = ""))

        assertFalse(accepts)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN checking THEN the draft is rejected`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val accepts = env.rule.accepts(StubTopic.stubContent())

        assertFalse(accepts)
    }

    @Test
    fun `GIVEN a guest and no characters WHEN checking THEN the draft is accepted`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val accepts = env.rule.accepts(
            StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist.")),
        )

        assertTrue(accepts)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val rule = TopicDraftRule(
            getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
        )
    }
}
