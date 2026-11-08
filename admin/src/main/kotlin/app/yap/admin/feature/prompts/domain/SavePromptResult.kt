package app.yap.admin.feature.prompts.domain

internal sealed interface SavePromptResult {

    data object Failed : SavePromptResult

    data object Rejected : SavePromptResult

    data class Saved(val prompt: Prompt) : SavePromptResult
}
