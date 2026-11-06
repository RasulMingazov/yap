package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.model.CreateCharacterOutcome
import app.yap.server.feature.characters.model.trimmedIfFilled
import app.yap.server.feature.characters.persistence.CharacterRepository

class CreateCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(profile: CharacterProfile): CreateCharacterOutcome {
        val filled = profile.trimmedIfFilled() ?: return CreateCharacterOutcome.Invalid
        return CreateCharacterOutcome.Created(characterRepository.create(filled))
    }
}
