package app.yap.server.feature.topics.model

import java.time.LocalDate

sealed interface Todo {

    val date: LocalDate

    data class Draft(
        override val date: LocalDate,
        val gaps: Set<TopicGap>,
        val title: String,
        val topicId: TopicId,
    ) : Todo

    data class EmptyDay(override val date: LocalDate) : Todo
}
