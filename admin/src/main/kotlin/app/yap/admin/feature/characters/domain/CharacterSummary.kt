package app.yap.admin.feature.characters.domain

internal data class CharacterSummary(
    val characterId: CharacterId,
    val profile: CharacterProfile,
    val topicCount: Int,
)
