package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TopicsUiStateMapperTest {

    @Test
    fun `GIVEN topics for two days WHEN mapping THEN the rows keep the server order with their short dates`() {
        val saturdayTopic = StubTopic.stubTopic(
            content = StubTopic.stubContent(releaseDate = LocalDate.of(2025, 10, 25)),
            topicId = "saturday-topic",
        )
        val state = DataState(topics = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic(), saturdayTopic)))

        val uiState = TopicsUiStateMapper(state)

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
    fun `GIVEN a topic without a release date WHEN mapping THEN its row has an empty date`() {
        val topic = StubTopic.stubTopic(content = StubTopic.stubContent(releaseDate = null))
        val state = DataState(topics = LoadTopicsResult.Loaded(listOf(topic)))

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = "", actual = assertIs<UiState.Content>(uiState).rows.single().date)
    }

    @Test
    fun `GIVEN no topics WHEN mapping THEN the screen says that there are none yet`() {
        val state = DataState(topics = LoadTopicsResult.Loaded(emptyList()))

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Empty(message = "Тем пока нет"), actual = uiState)
    }

    @Test
    fun `GIVEN the topics did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(topics = LoadTopicsResult.Failed)

        val uiState = TopicsUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить темы"), actual = uiState)
    }
}
