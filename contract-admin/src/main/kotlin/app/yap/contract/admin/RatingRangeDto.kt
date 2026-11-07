package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class RatingRangeDto(
    val from: Int,
    val to: Int?,
)
