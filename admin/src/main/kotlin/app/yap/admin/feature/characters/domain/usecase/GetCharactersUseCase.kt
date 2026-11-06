package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.repository.CharacterRepository

internal interface GetCharactersUseCase {

    suspend operator fun invoke(): LoadResult<List<Character>>
}

internal class DefaultGetCharactersUseCase(
    private val characterRepository: CharacterRepository,
) : GetCharactersUseCase {

    override suspend fun invoke(): LoadResult<List<Character>> = characterRepository.list()
}
