package app.yap.server.feature.prompts.model

sealed interface RestorePromptOutcome {

    data object NotFound : RestorePromptOutcome

    data class Restored(val prompt: Prompt) : RestorePromptOutcome
}
