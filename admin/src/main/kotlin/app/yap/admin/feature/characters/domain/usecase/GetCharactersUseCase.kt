package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.CharacterSummary
import app.yap.admin.feature.characters.domain.repository.CharacterRepository

internal interface GetCharactersUseCase {

    suspend operator fun invoke(): LoadResult<List<CharacterSummary>>
}

internal class DefaultGetCharactersUseCase(
    private val characterRepository: CharacterRepository,
) : GetCharactersUseCase {

    override suspend fun invoke(): LoadResult<List<CharacterSummary>> = characterRepository.list()
}
