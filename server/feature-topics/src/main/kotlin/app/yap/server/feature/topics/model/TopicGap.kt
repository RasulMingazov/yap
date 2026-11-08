package app.yap.server.feature.topics.model

sealed interface TopicGap {

    data class IncompleteSteps(val numbers: List<Int>) : TopicGap

    data class Missing(val field: TopicField) : TopicGap

    data object PastDate : TopicGap
}
