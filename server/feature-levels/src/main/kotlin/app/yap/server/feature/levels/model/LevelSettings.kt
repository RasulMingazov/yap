package app.yap.server.feature.levels.model

data class LevelSettings(
    val block: String,
    val idioms: Idioms,
    val maxWords: Int,
    val model: String?,
    val ratingFrom: Int?,
    val vocabulary: String,
) {

    fun normalized(): LevelSettings = copy(
        block = block.trim(),
        vocabulary = vocabulary.trim(),
    )

    internal val isFilled: Boolean
        get() = block.isNotBlank() && vocabulary.isNotBlank() && maxWords > 0
}
