package app.yap.server.feature.topics.model

sealed interface UpdateTopicOutcome {

    data object DateTaken : UpdateTopicOutcome

    data class Invalid(val gaps: Set<TopicGap>) : UpdateTopicOutcome

    data object NotFound : UpdateTopicOutcome

    data class Updated(val details: TopicDetails) : UpdateTopicOutcome
}
