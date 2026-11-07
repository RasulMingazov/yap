package app.yap.server.feature.topics.model

sealed interface UpdateTopicOutcome {

    data object Invalid : UpdateTopicOutcome

    data object NotFound : UpdateTopicOutcome

    data class Updated(val topic: Topic) : UpdateTopicOutcome
}
