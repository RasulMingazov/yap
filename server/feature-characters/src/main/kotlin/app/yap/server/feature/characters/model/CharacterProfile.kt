package app.yap.server.feature.characters.model

data class CharacterProfile(
    val history: String,
    val manner: String,
    val name: String,
    val personality: String,
)

fun CharacterProfile.trimmedIfFilled(): CharacterProfile? {
    val trimmed = CharacterProfile(
        history = history.trim(),
        manner = manner.trim(),
        name = name.trim(),
        personality = personality.trim(),
    )
    val fields = listOf(trimmed.history, trimmed.manner, trimmed.name, trimmed.personality)
    return trimmed.takeIf { fields.none(String::isEmpty) }
}
