package app.yap.server.feature.topics.model

sealed interface UpdateTopicOutcome {

    data object DateTaken : UpdateTopicOutcome

    data object Invalid : UpdateTopicOutcome

    data object NotFound : UpdateTopicOutcome

    data object StaleStatus : UpdateTopicOutcome

    data class Updated(val topic: Topic) : UpdateTopicOutcome
}
