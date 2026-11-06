package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val characterId: String,
    val profile: CharacterProfileDto,
)
