package app.yap.server.feature.models.model

enum class ModelTask {
    TOPIC_REPLY,
    TOPIC_CHECK,
    TOPIC_HINTS,
    TOPIC_REVIEW,
    CALIBRATION,
    ;

    val dependsOnLevel: Boolean
        get() = this == TOPIC_REPLY || this == TOPIC_HINTS
}
