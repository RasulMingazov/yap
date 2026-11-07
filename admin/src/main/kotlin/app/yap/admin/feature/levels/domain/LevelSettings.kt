package app.yap.admin.feature.levels.domain

internal data class LevelSettings(
    val block: String,
    val idioms: Idioms,
    val maxWords: Int,
    val ratingFrom: Int?,
    val vocabulary: String,
)
