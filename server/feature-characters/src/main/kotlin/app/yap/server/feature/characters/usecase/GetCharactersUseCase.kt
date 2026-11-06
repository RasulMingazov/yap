package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.persistence.CharacterRepository

class GetCharactersUseCase(
    private val characterRepository: CharacterRepository,
) {

    suspend operator fun invoke(): List<Character> = characterRepository.list()
}
