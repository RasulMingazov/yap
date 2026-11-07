package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class LevelDto(
    val code: String,
    val range: RatingRangeDto?,
    val settings: LevelSettingsDto?,
)
