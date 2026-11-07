package app.yap.admin.feature.levels.domain

internal sealed interface SaveLevelsResult {

    data object Failed : SaveLevelsResult

    data object Invalid : SaveLevelsResult

    data class Saved(val levels: List<Level>) : SaveLevelsResult
}
