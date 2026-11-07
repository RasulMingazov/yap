package app.yap.server.feature.prompts.model

data class Prompt(
    val current: PromptVersion?,
    val promptKey: PromptKey,
    val versions: List<PromptVersion>,
)
