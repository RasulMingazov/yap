package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.topics.domain.CalendarRow
import app.yap.admin.feature.topics.domain.LedBy
import app.yap.admin.feature.topics.domain.ReleaseState
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.Todo
import app.yap.admin.feature.topics.domain.TopicField
import app.yap.admin.feature.topics.domain.TopicGap
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.contract.admin.LedByDto
import app.yap.contract.admin.TodoDto
import app.yap.contract.admin.TopicFieldDto
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicStatusDto
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicCalendarMapperTest {

    @Test
    fun `GIVEN a day with a topic, an empty day and both To do entries WHEN mapping the calendar THEN each keeps its data`() {
        val dto = StubTopicCalendarDto.stubCalendarDto(
            rows = listOf(
                StubTopicCalendarDto.stubCalendarRowDto(
                    topic = StubTopicCalendarDto.stubCalendarTopicDto(
                        completeSteps = 2,
                        ledBy = LedByDto.CastDto(name = "Sam"),
                        status = TopicStatusDto.TODAY,
                        steps = 5,
                    ),
                ),
                StubTopicCalendarDto.stubCalendarRowDto(date = "2025-10-25", topic = null),
            ),
            todos = listOf(TodoDto.EmptyDayDto(date = "2025-10-25")),
        )

        val calendar = dto.toDomain()

        assertEquals(
            expected = StubTopicCalendar.stubCalendar(
                rows = listOf(
                    CalendarRow(
                        date = StubTopic.RELEASE_DATE,
                        topic = StubTopicCalendar.stubCalendarTopic(
                            completeSteps = 2,
                            ledBy = LedBy.Cast(name = "Sam"),
                            state = ReleaseState.TODAY,
                            steps = 5,
                        ),
                    ),
                    CalendarRow(date = StubTopic.RELEASE_DATE.plusDays(1), topic = null),
                ),
                todos = listOf(Todo.EmptyDay(date = StubTopic.RELEASE_DATE.plusDays(1))),
            ),
            actual = calendar,
        )
    }

    @Test
    fun `GIVEN a topic of each status WHEN mapping the calendar THEN each topic has its release state`() {
        val dto = StubTopicCalendarDto.stubCalendarDto(
            rows = TopicStatusDto.entries.map { status ->
                StubTopicCalendarDto.stubCalendarRowDto(
                    topic = StubTopicCalendarDto.stubCalendarTopicDto(status = status),
                )
            },
        )

        val calendar = dto.toDomain()

        assertEquals(
            expected = ReleaseState.entries,
            actual = calendar.rows.map { row -> row.topic?.state },
        )
    }

    @Test
    fun `GIVEN topics led by a character, by a guest and by no one WHEN mapping the calendar THEN each has its leader`() {
        val dto = StubTopicCalendarDto.stubCalendarDto(
            rows = listOf(LedByDto.CastDto(name = "Sam"), LedByDto.GuestDto, null).map { ledBy ->
                StubTopicCalendarDto.stubCalendarRowDto(
                    topic = StubTopicCalendarDto.stubCalendarTopicDto(ledBy = ledBy),
                )
            },
        )

        val calendar = dto.toDomain()

        assertEquals(
            expected = listOf(LedBy.Cast(name = "Sam"), LedBy.Guest, null),
            actual = calendar.rows.map { row -> row.topic?.ledBy },
        )
    }

    @Test
    fun `GIVEN a draft with every kind of gap WHEN mapping the calendar THEN the To do entry holds the draft and each gap`() {
        val dto = StubTopicCalendarDto.stubCalendarDto(
            todos = listOf(
                TodoDto.DraftDto(
                    date = StubTopicDto.RELEASE_DATE,
                    gaps = listOf(
                        TopicGapDto.DateTakenDto,
                        TopicGapDto.IncompleteStepsDto(numbers = listOf(3, 4)),
                        TopicGapDto.MissingDto(field = TopicFieldDto.TITLE),
                        TopicGapDto.PastDateDto,
                    ),
                    title = StubTopic.TITLE,
                    topicId = StubTopic.TOPIC_ID,
                ),
            ),
        )

        val calendar = dto.toDomain()

        assertEquals(
            expected = listOf(
                Todo.Draft(
                    date = StubTopic.RELEASE_DATE,
                    gaps = listOf(
                        TopicGap.DateTaken,
                        TopicGap.IncompleteSteps(numbers = listOf(3, 4)),
                        TopicGap.Missing(field = TopicField.TITLE),
                        TopicGap.PastDate,
                    ),
                    title = StubTopic.TITLE,
                    topicId = TopicId(StubTopic.TOPIC_ID),
                ),
            ),
            actual = calendar.todos,
        )
    }

    @Test
    fun `GIVEN a missing gap for each field WHEN mapping the calendar THEN each gap names its field`() {
        val dto = StubTopicCalendarDto.stubCalendarDto(
            todos = listOf(
                TodoDto.DraftDto(
                    date = StubTopicDto.RELEASE_DATE,
                    gaps = TopicFieldDto.entries.map { field -> TopicGapDto.MissingDto(field = field) },
                    title = StubTopic.TITLE,
                    topicId = StubTopic.TOPIC_ID,
                ),
            ),
        )

        val calendar = dto.toDomain()

        assertEquals(
            expected = TopicField.entries.map { field -> TopicGap.Missing(field = field) },
            actual = (calendar.todos.single() as Todo.Draft).gaps,
        )
    }
}
