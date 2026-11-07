package app.yap.server.feature.prompts.persistence

import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey

interface PromptRepository {

    suspend fun get(promptKey: PromptKey): Prompt

    suspend fun restore(promptKey: PromptKey, number: Int): Prompt?

    suspend fun save(promptKey: PromptKey, text: String): Prompt
}
