package app.yap.server.feature.topics.model

sealed interface CreateTopicOutcome {

    data class Created(val details: TopicDetails) : CreateTopicOutcome

    data class Invalid(val gaps: Set<TopicGap>) : CreateTopicOutcome
}
