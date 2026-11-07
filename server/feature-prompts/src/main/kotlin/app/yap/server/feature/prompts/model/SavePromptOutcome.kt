package app.yap.server.feature.prompts.model

sealed interface SavePromptOutcome {

    data object EmptyText : SavePromptOutcome

    data class Saved(val prompt: Prompt) : SavePromptOutcome
}
