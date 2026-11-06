package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfoDto(
    val environment: String,
)
