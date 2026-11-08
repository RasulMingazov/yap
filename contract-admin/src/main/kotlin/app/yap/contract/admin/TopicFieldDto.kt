package app.yap.contract.admin

import kotlinx.serialization.Serializable

@Serializable
enum class TopicFieldDto {
    TITLE,
    RELEASE_DATE,
    DURATION,
    DESCRIPTION,
    SETTING,
    INTERLOCUTOR,
    ROLE,
    OPENING_LINE,
    TRANSLATION,
    STEPS,
}
