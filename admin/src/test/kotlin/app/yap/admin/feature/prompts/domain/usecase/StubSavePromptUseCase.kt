package app.yap.admin.feature.prompts.domain.usecase

import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.StubPrompt
import io.github.rasulmingazov.stubcall.StubCall2

internal class StubSavePromptUseCase(
    result: SavePromptResult = SavePromptResult.Saved(StubPrompt.stubPrompt()),
) : SavePromptUseCase {

    val invokeCall = StubCall2.returns<PromptKey, String, SavePromptResult>(result)

    override suspend fun invoke(promptKey: PromptKey, text: String): SavePromptResult = invokeCall.invoke(
        promptKey,
        text,
    )
}
