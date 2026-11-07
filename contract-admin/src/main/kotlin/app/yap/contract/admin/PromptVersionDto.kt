package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class PromptVersionDto(
    val isCurrent: Boolean,
    val number: Int,
    val savedAt: String,
)
