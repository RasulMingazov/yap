package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.RestorePromptOutcome
import app.yap.server.feature.prompts.model.StubPrompt
import app.yap.server.feature.prompts.persistence.StubPromptRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class RestorePromptUseCaseTest {

    @Test
    fun `GIVEN a saved version WHEN restoring it THEN the prompt is returned`() = runTest {
        val env = Environment()

        val outcome = env.useCase(PromptKey.TOPIC_REPLY, StubPrompt.NUMBER)

        env.promptRepository.restoreCall.calledWith(PromptKey.TOPIC_REPLY, StubPrompt.NUMBER)
        assertEquals(expected = RestorePromptOutcome.Restored(StubPrompt.stubPrompt()), actual = outcome)
    }

    @Test
    fun `GIVEN an unknown version WHEN restoring it THEN it is not found`() = runTest {
        val env = Environment()
        env.promptRepository.restoreCall.returns(null)

        val outcome = env.useCase(PromptKey.TOPIC_REPLY, number = 3)

        assertEquals(expected = RestorePromptOutcome.NotFound, actual = outcome)
    }

    private class Environment {

        val promptRepository = StubPromptRepository()
        val useCase = RestorePromptUseCase(promptRepository = promptRepository)
    }
}
