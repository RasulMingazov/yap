package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterId

interface CharacterTopicCounts {

    suspend fun byCharacter(): Map<CharacterId, Int>
}
