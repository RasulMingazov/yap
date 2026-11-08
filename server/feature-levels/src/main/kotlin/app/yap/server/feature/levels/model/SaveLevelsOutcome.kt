package app.yap.server.feature.levels.model

sealed interface SaveLevelsOutcome {

    data object Invalid : SaveLevelsOutcome

    data object ModelUnavailable : SaveLevelsOutcome

    data class Saved(val levels: List<Level>) : SaveLevelsOutcome
}
