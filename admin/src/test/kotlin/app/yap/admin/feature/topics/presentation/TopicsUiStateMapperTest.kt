package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.topics.domain.LedBy
import app.yap.admin.feature.topics.domain.ReleaseState
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.Todo
import app.yap.admin.feature.topics.domain.TopicField
import app.yap.admin.feature.topics.domain.TopicGap
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TopicsUiStateMapperTest {

    @Test
    fun `GIVEN a topic and an empty day WHEN mapping THEN All topics keeps the server order`() {
        val saturday = StubTopic.RELEASE_DATE.plusDays(1)
        val published = StubTopicCalendar.stubCalendarTopic(
            completeSteps = 5,
            ledBy = LedBy.Cast(name = "Сэм"),
            state = ReleaseState.PUBLISHED,
            steps = 5,
        )
        val calendar = StubTopicCalendar.stubTopicCalendar(
            rows = listOf(
                StubTopicCalendar.stubCalendarRow(topic = published),
                StubTopicCalendar.stubCalendarRow(
                    date = saturday,
                    topic = null,
                ),
            ),
        )

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(expected = null, actual = uiState.reloadError)
        assertEquals(
            expected = listOf(
                UiState.Row.Topic(
                    date = "Пт, 24 окт",
                    ledBy = "Сэм",
                    status = Status(
                        text = "Опубликована",
                        tone = Status.Tone.SUCCESS,
                    ),
                    steps = "5",
                    title = "At the cafe",
                    topicId = TopicId(StubTopic.TOPIC_ID),
                ),
                UiState.Row.EmptyDay(
                    date = "Сб, 25 окт",
                    releaseDate = saturday,
                ),
            ),
            actual = uiState.rows,
        )
    }

    @Test
    fun `GIVEN a draft and an empty day WHEN mapping THEN To do keeps the server order`() {
        val saturday = StubTopic.RELEASE_DATE.plusDays(1)
        val calendar = StubTopicCalendar.stubTopicCalendar(
            todos = listOf(
                StubTopicCalendar.stubTodoDraft(),
                Todo.EmptyDay(date = saturday),
            ),
        )

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = UiState.TodoList.Entries(
                entries = listOf(
                    UiState.TodoEntry.Draft(
                        date = "Пт, 24 окт",
                        note = "Черновик. Опубликуйте.",
                        title = "At the cafe.",
                        topicId = TopicId(StubTopic.TOPIC_ID),
                    ),
                    UiState.TodoEntry.EmptyDay(
                        date = "Сб, 25 окт",
                        note = "В этот день пользователи увидят пустой день.",
                        releaseDate = saturday,
                        title = "Темы нет.",
                    ),
                ),
            ),
            actual = uiState.todo,
        )
    }

    @Test
    fun `GIVEN a topic of each release state WHEN mapping THEN each status has its text and tone`() {
        val calendar = StubTopicCalendar.stubTopicCalendar(
            rows = ReleaseState.entries.map { state ->
                StubTopicCalendar.stubCalendarRow(topic = StubTopicCalendar.stubCalendarTopic(state = state))
            },
        )

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = listOf(
                Status(
                    text = "Черновик",
                    tone = Status.Tone.WARNING,
                ),
                Status(
                    text = "Опубликована",
                    tone = Status.Tone.SUCCESS,
                ),
                Status(
                    text = "Сегодня",
                    tone = Status.Tone.SUCCESS,
                ),
                Status(
                    text = "Вышла",
                    tone = Status.Tone.MUTED,
                ),
            ),
            actual = uiState.rows.map { row -> assertIs<UiState.Row.Topic>(row).status },
        )
    }

    @Test
    fun `GIVEN a guest and no interlocutor WHEN mapping THEN Led by shows Guest and nothing`() {
        val calendar = StubTopicCalendar.stubTopicCalendar(
            rows = listOf(
                StubTopicCalendar.stubCalendarRow(topic = StubTopicCalendar.stubCalendarTopic(ledBy = LedBy.Guest)),
                StubTopicCalendar.stubCalendarRow(topic = StubTopicCalendar.stubCalendarTopic(ledBy = null)),
            ),
        )

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = listOf("Гость", ""),
            actual = uiState.rows.map { row -> assertIs<UiState.Row.Topic>(row).ledBy },
        )
    }

    @Test
    fun `GIVEN 5 steps and 2 complete steps WHEN mapping THEN Steps shows 2 of 5`() {
        val topic = StubTopicCalendar.stubCalendarTopic(
            completeSteps = 2,
            steps = 5,
        )
        val calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow(topic = topic)))

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = listOf("2 из 5"),
            actual = uiState.rows.map { row -> assertIs<UiState.Row.Topic>(row).steps },
        )
    }

    @Test
    fun `GIVEN a draft that publishing needs nothing more for WHEN mapping THEN To do says it is a draft to publish`() {
        val calendar = StubTopicCalendar.stubTopicCalendar(todos = listOf(StubTopicCalendar.stubTodoDraft()))

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = UiState.TodoList.Entries(
                entries = listOf(
                    UiState.TodoEntry.Draft(
                        date = "Пт, 24 окт",
                        note = "Черновик. Опубликуйте.",
                        title = "At the cafe.",
                        topicId = TopicId(StubTopic.TOPIC_ID),
                    ),
                ),
            ),
            actual = uiState.todo,
        )
    }

    @Test
    fun `GIVEN a draft without a description and with the steps 3–5 incomplete WHEN mapping THEN its note names both`() {
        val gaps = listOf(
            TopicGap.Missing(field = TopicField.DESCRIPTION),
            TopicGap.IncompleteSteps(numbers = listOf(3, 4, 5)),
        )
        val calendar = StubTopicCalendar.stubTopicCalendar(todos = listOf(StubTopicCalendar.stubTodoDraft(gaps = gaps)))

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(
            expected = UiState.TodoList.Entries(
                entries = listOf(
                    UiState.TodoEntry.Draft(
                        date = "Пт, 24 окт",
                        note = "Дописать описание и шаги 3–5.",
                        title = "At the cafe.",
                        topicId = TopicId(StubTopic.TOPIC_ID),
                    ),
                ),
            ),
            actual = uiState.todo,
        )
    }

    @Test
    fun `GIVEN a calendar without To do WHEN mapping THEN To do says that there is nothing to do`() {
        val calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow()))

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(expected = UiState.TodoList.Empty(message = "Всё сделано"), actual = uiState.todo)
    }

    @Test
    fun `GIVEN a kept calendar and a failed reload WHEN mapping THEN the banner shows above the kept calendar`() {
        val emptyDay = StubTopicCalendar.stubCalendarRow(topic = null)
        val state = DataState(
            calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(emptyDay)),
            isLoadFailed = true,
        )

        val uiState = TopicsUiStateMapper(state)

        assertEquals(
            expected = UiState.Content(
                reloadError = UiState.ReloadError(
                    isRetrying = false,
                    message = "Не удалось обновить темы. Показан прежний список.",
                ),
                rows = listOf(
                    UiState.Row.EmptyDay(
                        date = "Пт, 24 окт",
                        releaseDate = StubTopic.RELEASE_DATE,
                    ),
                ),
                todo = UiState.TodoList.Empty(message = "Всё сделано"),
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN a kept calendar and a retry after a failed reload WHEN mapping THEN the banner marks the retry`() {
        val state = DataState(
            calendar = StubTopicCalendar.stubTopicCalendar(),
            isLoadFailed = true,
            isLoading = true,
        )

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(state))

        assertEquals(
            expected = UiState.ReloadError(
                isRetrying = true,
                message = "Не удалось обновить темы. Показан прежний список.",
            ),
            actual = uiState.reloadError,
        )
    }

    @Test
    fun `GIVEN the calendar did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(isLoadFailed = true)

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить темы"), actual = uiState)
    }

    @Test
    fun `GIVEN no calendar and no failure WHEN mapping THEN the screen is loading`() {
        val state = DataState(isLoading = true)

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Loading, actual = uiState)
    }
}
