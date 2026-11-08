package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.UpdateCharacterOutcome
import app.yap.server.feature.characters.persistence.CharacterRepository

class UpdateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(character: Character): UpdateCharacterOutcome {
        val normalized = character.copy(profile = character.profile.normalized())
        if (!normalized.profile.isFilled) return UpdateCharacterOutcome.Invalid
        return if (characterRepository.update(normalized)) {
            UpdateCharacterOutcome.Updated(normalized)
        } else {
            UpdateCharacterOutcome.NotFound
        }
    }
}
