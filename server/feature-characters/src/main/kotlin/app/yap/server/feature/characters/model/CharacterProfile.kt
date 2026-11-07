package app.yap.server.feature.characters.model

data class CharacterProfile(
    val history: String,
    val manner: String,
    val name: String,
    val personality: String,
) {

    val isFilled: Boolean
        get() = listOf(history, manner, name, personality).none(String::isEmpty)
}
