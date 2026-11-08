package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.contract.admin.CalendarRowDto
import app.yap.contract.admin.CalendarTopicDto
import app.yap.contract.admin.LedByDto
import app.yap.contract.admin.TodoDto
import app.yap.contract.admin.TopicCalendarDto
import app.yap.contract.admin.TopicStatusDto

internal object StubTopicCalendarDto {

    fun stubCalendarDto(
        rows: List<CalendarRowDto> = emptyList(),
        todos: List<TodoDto> = emptyList(),
    ): TopicCalendarDto = TopicCalendarDto(
        rows = rows,
        todos = todos,
    )

    fun stubCalendarRowDto(
        date: String = StubTopicDto.RELEASE_DATE,
        topic: CalendarTopicDto? = stubCalendarTopicDto(),
    ): CalendarRowDto = CalendarRowDto(
        date = date,
        topic = topic,
    )

    fun stubCalendarTopicDto(
        completeSteps: Int = 1,
        ledBy: LedByDto? = null,
        status: TopicStatusDto = TopicStatusDto.DRAFT,
        steps: Int = 1,
        title: String = StubTopic.TITLE,
        topicId: String = StubTopic.TOPIC_ID,
    ): CalendarTopicDto = CalendarTopicDto(
        completeSteps = completeSteps,
        ledBy = ledBy,
        status = status,
        steps = steps,
        title = title,
        topicId = topicId,
    )
}
