package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
enum class TopicStatusDto {
    DRAFT,
    PUBLISHED,
    TODAY,
    OUT,
}
