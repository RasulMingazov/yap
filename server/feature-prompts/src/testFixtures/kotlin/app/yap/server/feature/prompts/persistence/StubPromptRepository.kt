package app.yap.server.feature.prompts.persistence

import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.StubPrompt
import io.github.rasulmingazov.stubcall.StubCall1
import io.github.rasulmingazov.stubcall.StubCall2

class StubPromptRepository(
    prompt: Prompt = StubPrompt.stubPrompt(),
) : PromptRepository {

    val getCall = StubCall1.returns<PromptKey, Prompt>(prompt)
    val restoreCall = StubCall2.returns<PromptKey, Int, Prompt?>(prompt)
    val saveCall = StubCall2.returns<PromptKey, String, Prompt>(prompt)

    override suspend fun get(promptKey: PromptKey): Prompt = getCall.invoke(promptKey)

    override suspend fun restore(promptKey: PromptKey, number: Int): Prompt? = restoreCall.invoke(promptKey, number)

    override suspend fun save(promptKey: PromptKey, text: String): Prompt = saveCall.invoke(promptKey, text)
}
