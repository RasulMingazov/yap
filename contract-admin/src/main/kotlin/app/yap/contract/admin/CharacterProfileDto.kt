package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class CharacterProfileDto(
    val history: String,
    val manner: String,
    val name: String,
    val personality: String,
)
