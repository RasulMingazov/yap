package app.yap.admin.feature.levels.domain

internal data class LevelInput(
    val block: String,
    val idioms: Idioms?,
    val maxWords: String,
    val model: String?,
    val ratingFrom: String,
    val vocabulary: String,
)
