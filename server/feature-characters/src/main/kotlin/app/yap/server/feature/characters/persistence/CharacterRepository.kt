package app.yap.server.feature.characters.persistence

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.model.CharacterProfile

interface CharacterRepository {

    suspend fun create(profile: CharacterProfile): Character

    suspend fun find(characterId: CharacterId): Character?

    suspend fun list(): List<Character>

    suspend fun update(character: Character): Boolean
}
