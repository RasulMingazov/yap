package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.StubPrompt
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetPromptsUseCase(
    summaries: LoadResult<List<PromptSummary>> = LoadResult.Loaded(StubPrompt.stubPromptSummaries()),
) : GetPromptsUseCase {

    val invokeCall = StubCall0.returns<LoadResult<List<PromptSummary>>>(summaries)

    override suspend fun invoke(): LoadResult<List<PromptSummary>> = invokeCall.invoke()
}
