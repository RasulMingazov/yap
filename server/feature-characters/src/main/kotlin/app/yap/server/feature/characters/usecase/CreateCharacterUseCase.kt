package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.model.CreateCharacterOutcome
import app.yap.server.feature.characters.persistence.CharacterRepository

class CreateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(profile: CharacterProfile): CreateCharacterOutcome {
        if (!profile.isFilled) return CreateCharacterOutcome.Invalid
        return CreateCharacterOutcome.Created(characterRepository.create(profile))
    }
}
