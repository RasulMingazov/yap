package app.yap.server.feature.characters.model

sealed interface UpdateCharacterOutcome {

    data object Invalid : UpdateCharacterOutcome

    data object NotFound : UpdateCharacterOutcome

    data class Updated(val character: Character) : UpdateCharacterOutcome
}
