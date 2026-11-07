package app.yap.contract.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface InterlocutorDto {

    @Serializable
    @SerialName("character")
    data class CharacterDto(val characterId: String) : InterlocutorDto

    @Serializable
    @SerialName("guest")
    data class GuestDto(val description: String) : InterlocutorDto
}
