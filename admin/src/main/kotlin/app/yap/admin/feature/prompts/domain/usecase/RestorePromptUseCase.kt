package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.repository.PromptRepository

internal interface RestorePromptUseCase {

    suspend operator fun invoke(promptKey: PromptKey, number: Int): RestorePromptResult
}

internal class DefaultRestorePromptUseCase(
    private val promptRepository: PromptRepository,
) : RestorePromptUseCase {

    override suspend fun invoke(promptKey: PromptKey, number: Int): RestorePromptResult = promptRepository.restore(
        promptKey = promptKey,
        number = number,
    )
}
