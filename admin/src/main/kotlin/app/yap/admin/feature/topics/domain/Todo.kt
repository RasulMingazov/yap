package app.yap.admin.feature.topics.domain

import java.time.LocalDate

internal sealed interface Todo {

    data class Draft(
        val date: LocalDate,
        val gaps: List<TopicGap>,
        val title: String,
        val topicId: TopicId,
    ) : Todo

    data class EmptyDay(val date: LocalDate) : Todo
}
