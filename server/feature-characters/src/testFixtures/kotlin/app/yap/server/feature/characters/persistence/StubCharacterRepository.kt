package app.yap.server.feature.characters.persistence

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.model.StubCharacter
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

class StubCharacterRepository(
    character: Character = StubCharacter.stubCharacter(),
) : CharacterRepository {

    val createCall = StubCall1.returns<CharacterProfile, Character>(character)
    val findCall = StubCall1.returns<CharacterId, Character?>(character)
    val listCall = StubCall0.returns<List<Character>>(listOf(character))
    val updateCall = StubCall1.returns<Character, Boolean>(true)

    override suspend fun create(profile: CharacterProfile): Character = createCall.invoke(profile)

    override suspend fun find(characterId: CharacterId): Character? = findCall.invoke(characterId)

    override suspend fun list(): List<Character> = listCall.invoke()

    override suspend fun update(character: Character): Boolean = updateCall.invoke(character)
}
