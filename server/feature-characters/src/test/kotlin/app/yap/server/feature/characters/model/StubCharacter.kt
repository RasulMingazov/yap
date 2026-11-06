package app.yap.server.feature.characters.model

import kotlin.uuid.Uuid

internal object StubCharacter {

    const val HISTORY = "Moved to the city five years ago. Drives a taxi and works in a cafe."
    const val MANNER = "Casual American English. Short phrases and contractions."
    const val NAME = "Sam"
    const val PERSONALITY = "Friendly and curious. Asks questions back."
    val CHARACTER_ID = CharacterId(Uuid.parse("2c7a5f1e-9a8b-4c3d-8e2f-1a2b3c4d5e6f"))

    fun stubCharacter(
        characterId: CharacterId = CHARACTER_ID,
        profile: CharacterProfile = stubProfile(),
    ): Character = Character(
        characterId = characterId,
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
