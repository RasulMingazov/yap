package app.yap.server.feature.topics.model

sealed interface CreateTopicOutcome {

    data class Created(val topic: Topic) : CreateTopicOutcome

    data object Invalid : CreateTopicOutcome
}
