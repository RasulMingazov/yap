package app.yap.admin.feature.prompts.domain

internal sealed interface SavePromptResult {

    data object EmptyText : SavePromptResult

    data object Failed : SavePromptResult

    data class Saved(val prompt: Prompt) : SavePromptResult
}
