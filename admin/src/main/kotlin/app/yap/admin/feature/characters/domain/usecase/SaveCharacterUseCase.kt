package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.repository.CharacterRepository

internal interface SaveCharacterUseCase {

    suspend operator fun invoke(characterId: CharacterId?, profile: CharacterProfile): SaveCharacterResult
}

internal class DefaultSaveCharacterUseCase(
    private val characterRepository: CharacterRepository,
) : SaveCharacterUseCase {

    override suspend fun invoke(characterId: CharacterId?, profile: CharacterProfile): SaveCharacterResult {
        if (characterId == null) return characterRepository.create(profile)
        return characterRepository.update(
            Character(
                characterId = characterId,
                profile = profile,
            ),
        )
    }
}
