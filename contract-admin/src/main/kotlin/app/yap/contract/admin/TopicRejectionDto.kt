package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
data class TopicRejectionDto(val gaps: List<TopicGapDto>)
