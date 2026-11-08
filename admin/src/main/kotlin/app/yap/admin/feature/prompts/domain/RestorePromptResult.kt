package app.yap.admin.feature.prompts.domain

internal sealed interface RestorePromptResult {

    data object Failed : RestorePromptResult

    data class Restored(val prompt: Prompt) : RestorePromptResult
}
