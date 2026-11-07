package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.UpdateCharacterOutcome
import app.yap.server.feature.characters.persistence.CharacterRepository

class UpdateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(character: Character): UpdateCharacterOutcome {
        if (!character.profile.isFilled) return UpdateCharacterOutcome.Invalid
        return if (characterRepository.update(character)) {
            UpdateCharacterOutcome.Updated(character)
        } else {
            UpdateCharacterOutcome.NotFound
        }
    }
}
