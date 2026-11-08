package app.yap.admin.feature.characters.domain

internal object StubCharacterSummary {

    fun stubCharacterSummary(
        characterId: String = StubCharacter.CHARACTER_ID,
        profile: CharacterProfile = StubCharacter.stubProfile(),
        topicCount: Int = 0,
    ): CharacterSummary = CharacterSummary(
        characterId = CharacterId(characterId),
        profile = profile,
        topicCount = topicCount,
    )
}
