package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.repository.PromptRepository

internal interface SavePromptUseCase {

    suspend operator fun invoke(promptKey: PromptKey, text: String): SavePromptResult
}

internal class DefaultSavePromptUseCase(
    private val promptRepository: PromptRepository,
) : SavePromptUseCase {

    override suspend fun invoke(promptKey: PromptKey, text: String): SavePromptResult = promptRepository.save(
        promptKey = promptKey,
        text = text,
    )
}
