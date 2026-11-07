package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class OpeningLineDto(
    val english: String,
    val translation: String,
)
