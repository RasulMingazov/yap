package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.SavePromptOutcome
import app.yap.server.feature.prompts.persistence.PromptRepository

class SavePromptUseCase(
    private val promptRepository: PromptRepository,
) {

    suspend operator fun invoke(promptKey: PromptKey, text: String): SavePromptOutcome {
        val normalized = text.trim()
        if (normalized.isEmpty()) return SavePromptOutcome.EmptyText
        return SavePromptOutcome.Saved(promptRepository.save(promptKey, normalized))
    }
}
