package app.yap.contract.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface LedByDto {

    @Serializable
    @SerialName("character")
    data class CastDto(val name: String) : LedByDto

    @Serializable
    @SerialName("guest")
    data object GuestDto : LedByDto
}
