package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.UpdateCharacterOutcome
import app.yap.server.feature.characters.model.trimmedIfFilled
import app.yap.server.feature.characters.persistence.CharacterRepository

class UpdateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(character: Character): UpdateCharacterOutcome {
        val filled = character.profile.trimmedIfFilled() ?: return UpdateCharacterOutcome.Invalid
        val updated = character.copy(profile = filled)
        return if (characterRepository.update(updated)) {
            UpdateCharacterOutcome.Updated(updated)
        } else {
            UpdateCharacterOutcome.NotFound
        }
    }
}
