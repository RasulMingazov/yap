package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.topics.domain.CalendarRow
import app.yap.admin.feature.topics.domain.CalendarTopic
import app.yap.admin.feature.topics.domain.LedBy
import app.yap.admin.feature.topics.domain.ReleaseState
import app.yap.admin.feature.topics.domain.Todo
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.TopicField
import app.yap.admin.feature.topics.domain.TopicGap
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.contract.admin.CalendarRowDto
import app.yap.contract.admin.CalendarTopicDto
import app.yap.contract.admin.LedByDto
import app.yap.contract.admin.TodoDto
import app.yap.contract.admin.TopicCalendarDto
import app.yap.contract.admin.TopicFieldDto
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicStatusDto
import java.time.LocalDate

internal fun TopicCalendarDto.toDomain(): TopicCalendar = TopicCalendar(
    rows = rows.map { row -> row.toDomain() },
    todos = todos.map { todo -> todo.toDomain() },
)

private fun CalendarRowDto.toDomain(): CalendarRow = CalendarRow(
    date = LocalDate.parse(date),
    topic = topic?.toDomain(),
)

private fun CalendarTopicDto.toDomain(): CalendarTopic = CalendarTopic(
    completeSteps = completeSteps,
    ledBy = ledBy?.toDomain(),
    state = status.toDomain(),
    steps = steps,
    title = title,
    topicId = TopicId(topicId),
)

private fun LedByDto.toDomain(): LedBy = when (this) {
    is LedByDto.CastDto -> LedBy.Cast(name = name)
    is LedByDto.GuestDto -> LedBy.Guest
}

internal fun TopicStatusDto.toDomain(): ReleaseState = when (this) {
    TopicStatusDto.DRAFT -> ReleaseState.DRAFT
    TopicStatusDto.PUBLISHED -> ReleaseState.PUBLISHED
    TopicStatusDto.TODAY -> ReleaseState.TODAY
    TopicStatusDto.OUT -> ReleaseState.OUT
}

private fun TodoDto.toDomain(): Todo = when (this) {
    is TodoDto.DraftDto -> Todo.Draft(
        date = LocalDate.parse(date),
        gaps = gaps.map { gap -> gap.toDomain() },
        title = title,
        topicId = TopicId(topicId),
    )
    is TodoDto.EmptyDayDto -> Todo.EmptyDay(date = LocalDate.parse(date))
}

internal fun TopicGapDto.toDomain(): TopicGap = when (this) {
    is TopicGapDto.DateTakenDto -> TopicGap.DateTaken
    is TopicGapDto.IncompleteStepsDto -> TopicGap.IncompleteSteps(numbers = numbers)
    is TopicGapDto.MissingDto -> TopicGap.Missing(field = field.toDomain())
    is TopicGapDto.PastDateDto -> TopicGap.PastDate
}

private fun TopicFieldDto.toDomain(): TopicField = when (this) {
    TopicFieldDto.TITLE -> TopicField.TITLE
    TopicFieldDto.RELEASE_DATE -> TopicField.RELEASE_DATE
    TopicFieldDto.DURATION -> TopicField.DURATION
    TopicFieldDto.DESCRIPTION -> TopicField.DESCRIPTION
    TopicFieldDto.SETTING -> TopicField.SETTING
    TopicFieldDto.INTERLOCUTOR -> TopicField.INTERLOCUTOR
    TopicFieldDto.ROLE -> TopicField.ROLE
    TopicFieldDto.OPENING_LINE -> TopicField.OPENING_LINE
    TopicFieldDto.TRANSLATION -> TopicField.TRANSLATION
    TopicFieldDto.STEPS -> TopicField.STEPS
}
