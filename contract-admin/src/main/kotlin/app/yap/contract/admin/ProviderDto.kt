package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class ProviderDto(
    val code: String,
    val connected: Boolean,
    val keySetting: String,
    val models: List<String>,
    val name: String,
)
