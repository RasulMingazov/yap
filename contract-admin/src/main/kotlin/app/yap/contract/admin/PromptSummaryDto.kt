package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class PromptSummaryDto(
    val group: String,
    val promptKey: String,
)
