package app.yap.admin.feature.topics.domain

internal sealed interface TopicGap {

    data object DateTaken : TopicGap

    data class IncompleteSteps(val numbers: List<Int>) : TopicGap

    data class Missing(val field: TopicField) : TopicGap

    data object PastDate : TopicGap
}
