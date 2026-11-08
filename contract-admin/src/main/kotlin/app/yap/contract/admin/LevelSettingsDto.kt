package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class LevelSettingsDto(
    val block: String,
    val idioms: String,
    val maxWords: Int,
    val ratingFrom: Int?,
    val vocabulary: String,
    val model: String? = null,
)
