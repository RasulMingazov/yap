package app.yap.server.feature.topics.model

sealed interface PublishTopicOutcome {

    data object DateTaken : PublishTopicOutcome

    data class Invalid(val gaps: Set<TopicGap>) : PublishTopicOutcome

    data object NotFound : PublishTopicOutcome

    data class Published(val topic: Topic) : PublishTopicOutcome
}
