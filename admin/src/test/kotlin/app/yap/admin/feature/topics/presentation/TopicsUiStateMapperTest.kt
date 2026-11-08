package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TopicsUiStateMapperTest {

    @Test
    fun `GIVEN topics on two days WHEN mapping THEN the rows keep the server order with their short dates`() {
        val saturday = StubTopicCalendar.stubCalendarRow(
            date = StubTopic.RELEASE_DATE.plusDays(1),
            topic = StubTopicCalendar.stubCalendarTopic(topicId = "saturday-topic"),
        )
        val calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow(), saturday))

        val uiState = TopicsUiStateMapper(DataState(calendar = calendar))

        assertEquals(
            expected = UiState.Content(
                rows = listOf(
                    UiState.Content.Row(
                        date = "Пт, 24 окт",
                        title = "At the cafe",
                        topicId = TopicId(StubTopic.TOPIC_ID),
                    ),
                    UiState.Content.Row(
                        date = "Сб, 25 окт",
                        title = "At the cafe",
                        topicId = TopicId("saturday-topic"),
                    ),
                ),
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN an empty day WHEN mapping THEN only the days with a topic have rows`() {
        val emptyDay = StubTopicCalendar.stubCalendarRow(
            date = StubTopic.RELEASE_DATE.plusDays(1),
            topic = null,
        )
        val calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow(), emptyDay))

        val uiState = assertIs<UiState.Content>(TopicsUiStateMapper(DataState(calendar = calendar)))

        assertEquals(expected = listOf("Пт, 24 окт"), actual = uiState.rows.map { row -> row.date })
    }

    @Test
    fun `GIVEN no topics WHEN mapping THEN the screen says that there are none yet`() {
        val state = DataState(calendar = StubTopicCalendar.stubTopicCalendar())

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Empty(message = "Тем пока нет"), actual = uiState)
    }

    @Test
    fun `GIVEN the calendar did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(isLoadFailed = true)

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить темы"), actual = uiState)
    }

    @Test
    fun `GIVEN a kept calendar and a failed reload WHEN mapping THEN the screen shows the kept calendar`() {
        val calendar = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow()))
        val state = DataState(
            calendar = calendar,
            isLoadFailed = true,
        )

        val uiState = TopicsUiStateMapper(state)

        assertIs<UiState.Content>(uiState)
    }

    @Test
    fun `GIVEN no calendar and no failure WHEN mapping THEN the screen is loading`() {
        val state = DataState(isLoading = true)

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Loading, actual = uiState)
    }
}
