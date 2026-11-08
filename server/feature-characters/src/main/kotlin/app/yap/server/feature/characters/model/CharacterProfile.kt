package app.yap.server.feature.characters.model

data class CharacterProfile(
    val history: String,
    val manner: String,
    val name: String,
    val personality: String,
) {

    internal val isFilled: Boolean
        get() = listOf(history, manner, name, personality).none(String::isEmpty)

    internal fun normalized(): CharacterProfile = CharacterProfile(
        history = history.trim(),
        manner = manner.trim(),
        name = name.trim(),
        personality = personality.trim(),
    )
}
