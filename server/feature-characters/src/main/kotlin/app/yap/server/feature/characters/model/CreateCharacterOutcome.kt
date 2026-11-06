package app.yap.server.feature.characters.model

sealed interface CreateCharacterOutcome {

    data class Created(val character: Character) : CreateCharacterOutcome

    data object Invalid : CreateCharacterOutcome
}
