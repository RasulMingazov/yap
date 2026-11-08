package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.CalendarRow
import app.yap.admin.feature.topics.domain.CalendarTopic
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import java.time.LocalDate

internal object TopicsUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState {
        val calendar = state.calendar
        return when {
            calendar != null -> calendar.toUiState()
            state.isLoadFailed -> UiState.Error(message = "Не удалось загрузить темы")
            else -> UiState.Loading
        }
    }

    private fun TopicCalendar.toUiState(): UiState {
        val topicRows = rows.mapNotNull { row -> row.toRow() }
        if (topicRows.isEmpty()) return UiState.Empty(message = "Тем пока нет")
        return UiState.Content(rows = topicRows)
    }

    private fun CalendarRow.toRow(): UiState.Content.Row? = topic?.toRow(date)

    private fun CalendarTopic.toRow(date: LocalDate): UiState.Content.Row = UiState.Content.Row(
        date = ReleaseDateFormat.short(date),
        title = title,
        topicId = topicId,
    )
}
