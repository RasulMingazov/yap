package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.RestorePromptOutcome
import app.yap.server.feature.prompts.persistence.PromptRepository

class RestorePromptUseCase(
    private val promptRepository: PromptRepository,
) {

    suspend operator fun invoke(promptKey: PromptKey, number: Int): RestorePromptOutcome {
        val prompt = promptRepository.restore(promptKey, number) ?: return RestorePromptOutcome.NotFound
        return RestorePromptOutcome.Restored(prompt)
    }
}
