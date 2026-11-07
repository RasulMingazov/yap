package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import java.time.LocalDate

internal object TopicsUiStateMapper {

    fun map(state: DataState): UiState = when (val result = state.topics) {
        null -> UiState.Loading
        is LoadTopicsResult.Failed -> UiState.Error(message = "Не удалось загрузить темы")
        is LoadTopicsResult.Loaded -> result.topics.toUiState()
    }

    private fun List<Topic>.toUiState(): UiState {
        if (isEmpty()) return UiState.Empty(message = "Тем пока нет")
        return UiState.Content(rows = map { topic -> topic.toRow() })
    }

    private fun Topic.toRow(): UiState.Content.Row = UiState.Content.Row(
        date = content.releaseDate.shortText(),
        title = content.title,
        topicId = topicId,
    )

    private fun LocalDate?.shortText(): String = this?.let(ReleaseDateFormat::short).orEmpty()
}
