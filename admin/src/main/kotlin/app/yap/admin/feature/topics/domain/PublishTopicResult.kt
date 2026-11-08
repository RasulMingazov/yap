package app.yap.admin.feature.topics.domain

internal sealed interface PublishTopicResult {

    data object DateTaken : PublishTopicResult

    data object Failed : PublishTopicResult

    data class Invalid(val gaps: List<TopicGap>) : PublishTopicResult

    data object NotFound : PublishTopicResult

    data class Published(val topic: Topic) : PublishTopicResult
}
