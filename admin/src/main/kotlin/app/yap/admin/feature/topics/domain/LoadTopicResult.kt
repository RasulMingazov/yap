package app.yap.admin.feature.topics.domain

internal sealed interface LoadTopicResult {

    data object Failed : LoadTopicResult

    data class Loaded(val topic: Topic) : LoadTopicResult
}
