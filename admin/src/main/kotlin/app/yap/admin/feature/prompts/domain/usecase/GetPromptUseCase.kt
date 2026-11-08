package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.repository.PromptRepository

internal interface GetPromptUseCase {

    suspend operator fun invoke(promptKey: PromptKey): LoadResult<Prompt>
}

internal class DefaultGetPromptUseCase(
    private val promptRepository: PromptRepository,
) : GetPromptUseCase {

    override suspend fun invoke(promptKey: PromptKey): LoadResult<Prompt> = promptRepository.find(promptKey)
}
