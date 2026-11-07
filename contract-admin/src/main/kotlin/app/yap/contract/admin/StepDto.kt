package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class StepDto(
    val outcome: String,
    val passWhen: String,
    val task: String,
)
