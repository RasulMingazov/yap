package app.yap.server.feature.topics.api

import app.yap.contract.admin.CalendarRowDto
import app.yap.contract.admin.CalendarTopicDto
import app.yap.contract.admin.LedByDto
import app.yap.contract.admin.TodoDto
import app.yap.contract.admin.TopicCalendarDto
import app.yap.server.feature.topics.model.CalendarRow
import app.yap.server.feature.topics.model.CalendarTopic
import app.yap.server.feature.topics.model.LedBy
import app.yap.server.feature.topics.model.Todo
import app.yap.server.feature.topics.model.TopicCalendar

internal fun TopicCalendar.toDto(): TopicCalendarDto = TopicCalendarDto(
    rows = rows.map { row -> row.toDto() },
    todos = todos.map { todo -> todo.toDto() },
)

private fun CalendarRow.toDto(): CalendarRowDto = CalendarRowDto(
    date = date.toString(),
    topic = topic?.toDto(),
)

private fun CalendarTopic.toDto(): CalendarTopicDto = CalendarTopicDto(
    completeSteps = completeSteps,
    ledBy = ledBy?.toDto(),
    status = state.toDto(),
    steps = steps,
    title = title,
    topicId = topicId.value.toString(),
)

private fun LedBy.toDto(): LedByDto = when (this) {
    is LedBy.Cast -> LedByDto.CastDto(name = name)
    is LedBy.Guest -> LedByDto.GuestDto
}

private fun Todo.toDto(): TodoDto = when (this) {
    is Todo.Draft -> TodoDto.DraftDto(
        date = date.toString(),
        gaps = gaps.toDto(),
        title = title,
        topicId = topicId.value.toString(),
    )
    is Todo.EmptyDay -> TodoDto.EmptyDayDto(date = date.toString())
}
