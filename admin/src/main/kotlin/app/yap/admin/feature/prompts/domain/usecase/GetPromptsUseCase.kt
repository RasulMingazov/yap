package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.repository.PromptRepository

internal interface GetPromptsUseCase {

    suspend operator fun invoke(): LoadResult<List<PromptSummary>>
}

internal class DefaultGetPromptsUseCase(
    private val promptRepository: PromptRepository,
) : GetPromptsUseCase {

    override suspend fun invoke(): LoadResult<List<PromptSummary>> = promptRepository.list()
}
