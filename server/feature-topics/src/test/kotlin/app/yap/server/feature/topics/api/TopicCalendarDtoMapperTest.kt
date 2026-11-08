package app.yap.server.feature.topics.api

import app.yap.contract.admin.CalendarRowDto
import app.yap.contract.admin.CalendarTopicDto
import app.yap.contract.admin.LedByDto
import app.yap.contract.admin.TodoDto
import app.yap.contract.admin.TopicCalendarDto
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicStatusDto
import app.yap.server.feature.topics.model.CalendarRow
import app.yap.server.feature.topics.model.LedBy
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Todo
import app.yap.server.feature.topics.model.TopicGap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicCalendarDtoMapperTest {

    @Test
    fun `GIVEN a row with a topic and an empty row WHEN mapping the calendar THEN both rows keep their data`() {
        val calendar = StubTopic.stubCalendar(
            rows = listOf(
                CalendarRow(
                    date = StubTopic.RELEASE_DATE,
                    topic = StubTopic.stubCalendarTopic(
                        completeSteps = 2,
                        ledBy = LedBy.Cast(name = "Sam"),
                        state = ReleaseState.TODAY,
                        steps = 5,
                    ),
                ),
                CalendarRow(date = StubTopic.RELEASE_DATE.plusDays(1), topic = null),
            ),
        )

        val dto = calendar.toDto()

        assertEquals(
            expected = TopicCalendarDto(
                rows = listOf(
                    CalendarRowDto(
                        date = StubTopicDto.RELEASE_DATE,
                        topic = CalendarTopicDto(
                            completeSteps = 2,
                            ledBy = LedByDto.CastDto(name = "Sam"),
                            status = TopicStatusDto.TODAY,
                            steps = 5,
                            title = StubTopic.TITLE,
                            topicId = StubTopicDto.TOPIC_ID,
                        ),
                    ),
                    CalendarRowDto(date = "2026-10-25", topic = null),
                ),
                todos = emptyList(),
            ),
            actual = dto,
        )
    }

    @Test
    fun `GIVEN topics led by a guest and by no one WHEN mapping the calendar THEN one is led by a guest and the other by no one`() {
        val calendar = StubTopic.stubCalendar(
            rows = listOf(
                CalendarRow(date = StubTopic.RELEASE_DATE, topic = StubTopic.stubCalendarTopic(ledBy = LedBy.Guest)),
                CalendarRow(date = StubTopic.RELEASE_DATE, topic = StubTopic.stubCalendarTopic(ledBy = null)),
            ),
        )

        val dto = calendar.toDto()

        assertEquals(
            expected = listOf(LedByDto.GuestDto, null),
            actual = dto.rows.map { row -> row.topic?.ledBy },
        )
    }

    @Test
    fun `GIVEN a draft with gaps and an empty day WHEN mapping the calendar THEN both to do entries keep their data`() {
        val calendar = StubTopic.stubCalendar(
            todos = listOf(
                Todo.Draft(
                    date = StubTopic.RELEASE_DATE,
                    gaps = setOf(TopicGap.PastDate),
                    title = StubTopic.TITLE,
                    topicId = StubTopic.TOPIC_ID,
                ),
                Todo.EmptyDay(date = StubTopic.RELEASE_DATE.plusDays(1)),
            ),
        )

        val dto = calendar.toDto()

        assertEquals(
            expected = listOf(
                TodoDto.DraftDto(
                    date = StubTopicDto.RELEASE_DATE,
                    gaps = listOf(TopicGapDto.PastDateDto),
                    title = StubTopic.TITLE,
                    topicId = StubTopicDto.TOPIC_ID,
                ),
                TodoDto.EmptyDayDto(date = "2026-10-25"),
            ),
            actual = dto.todos,
        )
    }
}
