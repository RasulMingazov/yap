package app.yap.admin.feature.characters.domain.repository

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.SaveCharacterResult

internal interface CharacterRepository {

    suspend fun create(profile: CharacterProfile): SaveCharacterResult

    suspend fun find(characterId: CharacterId): LoadResult<Character>

    suspend fun list(): LoadResult<List<Character>>

    suspend fun update(character: Character): SaveCharacterResult
}
