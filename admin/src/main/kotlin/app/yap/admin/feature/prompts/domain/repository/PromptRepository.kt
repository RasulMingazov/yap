package app.yap.admin.feature.prompts.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.SavePromptResult

internal interface PromptRepository {

    suspend fun find(promptKey: PromptKey): LoadResult<Prompt>

    suspend fun list(): LoadResult<List<PromptSummary>>

    suspend fun restore(promptKey: PromptKey, number: Int): RestorePromptResult

    suspend fun save(promptKey: PromptKey, text: String): SavePromptResult
}
