package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class CharacterSummaryDto(
    val characterId: String,
    val profile: CharacterProfileDto,
    val topicCount: Int,
)
