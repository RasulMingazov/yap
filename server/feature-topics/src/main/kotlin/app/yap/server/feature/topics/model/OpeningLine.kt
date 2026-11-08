package app.yap.server.feature.topics.model

data class OpeningLine(
    val english: String,
    val translation: String,
) {

    internal fun normalized(): OpeningLine = copy(
        english = english.trim(),
        translation = translation.trim(),
    )
}
