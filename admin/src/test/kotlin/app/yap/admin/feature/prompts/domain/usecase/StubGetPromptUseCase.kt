package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.StubPrompt
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubGetPromptUseCase(
    prompt: LoadResult<Prompt> = LoadResult.Loaded(StubPrompt.stubPrompt()),
) : GetPromptUseCase {

    val invokeCall = StubCall1.returns<PromptKey, LoadResult<Prompt>>(prompt)

    override suspend fun invoke(promptKey: PromptKey): LoadResult<Prompt> = invokeCall.invoke(promptKey)
}
