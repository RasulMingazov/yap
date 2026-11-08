package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.StubPrompt
import io.github.rasulmingazov.stubcall.StubCall2

internal class StubRestorePromptUseCase(
    result: RestorePromptResult = RestorePromptResult.Restored(StubPrompt.stubPrompt()),
) : RestorePromptUseCase {

    val invokeCall = StubCall2.returns<PromptKey, Int, RestorePromptResult>(result)

    override suspend fun invoke(promptKey: PromptKey, number: Int): RestorePromptResult = invokeCall.invoke(
        promptKey,
        number,
    )
}
