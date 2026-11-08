package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class TaskModelDto(
    val model: String,
    val provider: String,
    val task: String,
)
