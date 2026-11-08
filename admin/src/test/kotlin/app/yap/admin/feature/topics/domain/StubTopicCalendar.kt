package app.yap.admin.feature.topics.domain

import java.time.LocalDate

internal object StubTopicCalendar {

    fun stubTopicCalendar(
        rows: List<CalendarRow> = emptyList(),
        todos: List<Todo> = emptyList(),
    ): TopicCalendar = TopicCalendar(
        rows = rows,
        todos = todos,
    )

    fun stubCalendarRow(
        date: LocalDate = StubTopic.RELEASE_DATE,
        topic: CalendarTopic? = stubCalendarTopic(),
    ): CalendarRow = CalendarRow(
        date = date,
        topic = topic,
    )

    fun stubCalendarTopic(
        completeSteps: Int = 1,
        ledBy: LedBy? = null,
        state: ReleaseState = ReleaseState.DRAFT,
        steps: Int = 1,
        title: String = StubTopic.TITLE,
        topicId: String = StubTopic.TOPIC_ID,
    ): CalendarTopic = CalendarTopic(
        completeSteps = completeSteps,
        ledBy = ledBy,
        state = state,
        steps = steps,
        title = title,
        topicId = TopicId(topicId),
    )
}
