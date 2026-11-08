package app.yap.admin.feature.levels.domain

internal enum class LevelCode {
    A1, A2, B1, B2, C1, C2, NO_RATING;

    val hasRatingFrom: Boolean
        get() = this != NO_RATING
}
