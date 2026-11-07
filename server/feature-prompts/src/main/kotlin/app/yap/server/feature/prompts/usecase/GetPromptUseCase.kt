package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.persistence.PromptRepository

class GetPromptUseCase(
    private val promptRepository: PromptRepository,
) {

    suspend operator fun invoke(promptKey: PromptKey): Prompt = promptRepository.get(promptKey)
}
