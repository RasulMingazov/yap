package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TopicsUiStateMapperTest {

    @Test
    fun `GIVEN loaded topics WHEN mapping THEN each topic is a row with its short date and title`() {
        val state = DataState(topics = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())))

        val uiState = TopicsUiStateMapper.map(state)

        assertEquals(
            expected = UiState.Content(
                rows = listOf(
                    UiState.Content.Row(
                        date = "Пт, 24 окт",
                        title = "At the cafe",
                        topicId = TopicId(StubTopic.TOPIC_ID),
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

        val uiState = TopicsUiStateMapper.map(state)

        assertEquals(expected = "", actual = assertIs<UiState.Content>(uiState).rows.single().date)
    }

    @Test
    fun `GIVEN no topics WHEN mapping THEN the screen says that there are none yet`() {
        val state = DataState(topics = LoadTopicsResult.Loaded(emptyList()))

        val uiState = TopicsUiStateMapper.map(state)

        assertEquals(expected = UiState.Empty(message = "Тем пока нет"), actual = uiState)
    }

    @Test
    fun `GIVEN the topics did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(topics = LoadTopicsResult.Failed)

        val uiState = TopicsUiStateMapper.map(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить темы"), actual = uiState)
    }
}
