package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterId
import io.github.rasulmingazov.stubcall.StubCall0

class StubCharacterTopicCounts : CharacterTopicCounts {

    val byCharacterCall = StubCall0.returns<Map<CharacterId, Int>>(emptyMap())

    override suspend fun byCharacter(): Map<CharacterId, Int> = byCharacterCall.invoke()
}
