package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.persistence.CharacterRepository

class GetCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(characterId: CharacterId): Character? = characterRepository.find(characterId)
}
