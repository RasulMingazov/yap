package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class PromptTextDto(
    val text: String,
)
