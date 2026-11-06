package app.yap.admin.feature.characters.domain

internal object StubCharacter {

    const val CHARACTER_ID = "2c7a5f1e-9a8b-4c3d-8e2f-1a2b3c4d5e6f"
    const val HISTORY = "Moved to the city five years ago."
    const val MANNER = "Casual American English."
    const val NAME = "Sam"
    const val PERSONALITY = "Friendly and curious."

    fun stubCharacter(
        characterId: String = CHARACTER_ID,
        profile: CharacterProfile = stubProfile(),
    ): Character = Character(
        characterId = CharacterId(characterId),
        profile = profile,
    )

    fun stubProfile(
        history: String = HISTORY,
        manner: String = MANNER,
        name: String = NAME,
        personality: String = PERSONALITY,
    ): CharacterProfile = CharacterProfile(
        history = history,
        manner = manner,
        name = name,
        personality = personality,
    )
}
