package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.repository.CharacterRepository

internal interface GetCharacterUseCase {

    suspend operator fun invoke(characterId: CharacterId): LoadResult<Character>
}

internal class DefaultGetCharacterUseCase(
    private val characterRepository: CharacterRepository,
) : GetCharacterUseCase {

    override suspend fun invoke(characterId: CharacterId): LoadResult<Character> = characterRepository.find(characterId)
}
