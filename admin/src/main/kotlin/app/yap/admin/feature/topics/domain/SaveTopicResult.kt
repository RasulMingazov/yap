package app.yap.admin.feature.topics.domain

internal sealed interface SaveTopicResult {

    data object Failed : SaveTopicResult

    data object Invalid : SaveTopicResult

    data class Saved(val topic: Topic) : SaveTopicResult
}
