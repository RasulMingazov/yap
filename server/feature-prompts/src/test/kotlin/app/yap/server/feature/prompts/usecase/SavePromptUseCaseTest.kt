package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.SavePromptOutcome
import app.yap.server.feature.prompts.model.StubPrompt
import app.yap.server.feature.prompts.persistence.StubPromptRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class SavePromptUseCaseTest {

    @Test
    fun `GIVEN a text WHEN saving a prompt THEN the text is stored and the prompt is returned`() = runTest {
        val env = Environment()

        val outcome = env.useCase(PromptKey.TOPIC_REPLY, StubPrompt.TEXT)

        env.promptRepository.saveCall.calledWith(PromptKey.TOPIC_REPLY, StubPrompt.TEXT)
        assertEquals(expected = SavePromptOutcome.Saved(StubPrompt.stubPrompt()), actual = outcome)
    }

    @Test
    fun `GIVEN an empty text WHEN saving a prompt THEN nothing is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(PromptKey.TOPIC_REPLY, "")

        env.promptRepository.saveCall.notCalled()
        assertEquals(expected = SavePromptOutcome.EmptyText, actual = outcome)
    }

    private class Environment {

        val promptRepository = StubPromptRepository()
        val useCase = SavePromptUseCase(promptRepository = promptRepository)
    }
}
