package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.CharacterSummary
import app.yap.admin.feature.characters.domain.StubCharacterSummary
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetCharactersUseCase(
    characters: LoadResult<List<CharacterSummary>> = LoadResult.Loaded(
        listOf(StubCharacterSummary.stubCharacterSummary()),
    ),
) : GetCharactersUseCase {

    val invokeCall = StubCall0.returns<LoadResult<List<CharacterSummary>>>(characters)

    override suspend fun invoke(): LoadResult<List<CharacterSummary>> = invokeCall.invoke()
}
