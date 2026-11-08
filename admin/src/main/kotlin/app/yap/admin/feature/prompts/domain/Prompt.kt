package app.yap.admin.feature.prompts.domain

internal data class Prompt(
    val promptKey: PromptKey,
    val text: String?,
    val variables: List<String>,
    val versions: List<PromptVersion>,
)
