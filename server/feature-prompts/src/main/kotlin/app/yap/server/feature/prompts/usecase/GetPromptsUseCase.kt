package app.yap.server.feature.prompts.usecase

import app.yap.server.feature.prompts.model.PromptKey

class GetPromptsUseCase {

    suspend operator fun invoke(): List<PromptKey> = PromptKey.entries
}
