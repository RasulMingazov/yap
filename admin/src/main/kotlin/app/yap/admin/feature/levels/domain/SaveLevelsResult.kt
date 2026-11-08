package app.yap.admin.feature.levels.domain

internal sealed interface SaveLevelsResult {

    data object Failed : SaveLevelsResult

    data object Invalid : SaveLevelsResult

    data object ModelUnavailable : SaveLevelsResult

    data class NoIdioms(val code: LevelCode) : SaveLevelsResult

    data class NoWordCount(val code: LevelCode) : SaveLevelsResult

    data class Saved(val levels: List<Level>) : SaveLevelsResult

    data class UnreadNumber(
        val code: LevelCode,
        val field: NumberField,
    ) : SaveLevelsResult

    enum class NumberField {
        MAX_WORDS,
        RATING_FROM,
    }
}
