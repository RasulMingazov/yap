package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class ModelsDto(
    val providers: List<ProviderDto>,
    val tasks: List<TaskModelDto>,
)
