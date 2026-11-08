package app.yap.admin.feature.topics.domain

internal sealed interface SaveTopicResult {

    data object DateTaken : SaveTopicResult

    data object Failed : SaveTopicResult

    data class Invalid(val gaps: List<TopicGap>) : SaveTopicResult

    data class Saved(val topic: Topic) : SaveTopicResult
}
