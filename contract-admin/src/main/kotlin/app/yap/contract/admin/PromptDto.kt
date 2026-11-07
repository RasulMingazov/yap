package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class PromptDto(
    val group: String,
    val promptKey: String,
    val text: String?,
    val variables: List<String>,
    val versions: List<PromptVersionDto>,
)
