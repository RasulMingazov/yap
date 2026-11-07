package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class LevelInputDto(
    val code: String,
    val settings: LevelSettingsDto,
)
