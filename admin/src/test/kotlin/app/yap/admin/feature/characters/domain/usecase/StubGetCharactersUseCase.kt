package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.StubCharacter
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetCharactersUseCase(
    characters: LoadResult<List<Character>> = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())),
) : GetCharactersUseCase {

    val invokeCall = StubCall0.returns<LoadResult<List<Character>>>(characters)

    override suspend fun invoke(): LoadResult<List<Character>> = invokeCall.invoke()
}
