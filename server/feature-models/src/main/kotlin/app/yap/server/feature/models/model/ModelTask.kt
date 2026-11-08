package app.yap.server.feature.models.model

enum class ModelTask(
    internal val dependsOnLevel: Boolean = false,
) {
    TOPIC_REPLY(dependsOnLevel = true),
    TOPIC_CHECK,
    TOPIC_HINTS(dependsOnLevel = true),
    TOPIC_REVIEW,
    CALIBRATION,
}
