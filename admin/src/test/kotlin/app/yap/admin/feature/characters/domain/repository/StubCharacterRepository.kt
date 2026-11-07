package app.yap.admin.feature.characters.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubCharacterRepository(
    character: Character = StubCharacter.stubCharacter(),
) : CharacterRepository {

    val createCall = StubCall1.returns<CharacterProfile, SaveCharacterResult>(SaveCharacterResult.Saved(character))
    val findCall = StubCall1.returns<CharacterId, LoadResult<Character>>(LoadResult.Loaded(character))
    val listCall = StubCall0.returns<LoadResult<List<Character>>>(LoadResult.Loaded(listOf(character)))
    val updateCall = StubCall1.returns<Character, SaveCharacterResult>(SaveCharacterResult.Saved(character))

    override suspend fun create(profile: CharacterProfile): SaveCharacterResult = createCall.invoke(profile)

    override suspend fun find(characterId: CharacterId): LoadResult<Character> = findCall.invoke(characterId)

    override suspend fun list(): LoadResult<List<Character>> = listCall.invoke()

    override suspend fun update(character: Character): SaveCharacterResult = updateCall.invoke(character)
}
