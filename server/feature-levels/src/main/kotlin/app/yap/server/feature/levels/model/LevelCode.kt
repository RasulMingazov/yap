package app.yap.server.feature.levels.model

enum class LevelCode {
    A1, A2, B1, B2, C1, C2, NO_RATING;

    internal val isRated: Boolean
        get() = this != NO_RATING

    internal val nextRated: LevelCode?
        get() = entries.firstOrNull { code -> code.isRated && code.ordinal > ordinal }
}
