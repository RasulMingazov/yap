package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.StubCharacter
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubGetCharacterUseCase(
    character: LoadResult<Character> = LoadResult.Loaded(StubCharacter.stubCharacter()),
) : GetCharacterUseCase {

    val invokeCall = StubCall1.returns<CharacterId, LoadResult<Character>>(character)

    override suspend fun invoke(characterId: CharacterId): LoadResult<Character> = invokeCall.invoke(characterId)
}
