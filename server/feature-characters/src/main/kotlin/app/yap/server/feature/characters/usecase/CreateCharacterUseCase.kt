package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.model.CreateCharacterOutcome
import app.yap.server.feature.characters.persistence.CharacterRepository

class CreateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(profile: CharacterProfile): CreateCharacterOutcome {
        val normalized = profile.normalized()
        if (!normalized.isFilled) return CreateCharacterOutcome.Invalid
        return CreateCharacterOutcome.Created(characterRepository.create(normalized))
    }
}
