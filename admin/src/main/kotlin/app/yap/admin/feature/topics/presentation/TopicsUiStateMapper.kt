package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.topics.domain.CalendarRow
import app.yap.admin.feature.topics.domain.CalendarTopic
import app.yap.admin.feature.topics.domain.LedBy
import app.yap.admin.feature.topics.domain.ReleaseState
import app.yap.admin.feature.topics.domain.Todo
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.presentation.TopicsViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import java.time.LocalDate

internal object TopicsUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState {
        val calendar = state.calendar
        return when {
            calendar != null -> state.toContent(calendar)
            state.isLoadFailed && !state.isLoading -> UiState.Error(message = "Не удалось загрузить темы")
            else -> UiState.Loading
        }
    }

    private fun DataState.toContent(calendar: TopicCalendar): UiState.Content = UiState.Content(
        reloadError = reloadError(),
        rows = calendar.rows.map { row -> row.toRow() },
        todo = calendar.todos.toTodoList(),
    )

    private fun DataState.reloadError(): UiState.ReloadError? {
        if (!isLoadFailed) return null
        return UiState.ReloadError(
            isRetrying = isLoading,
            message = "Не удалось обновить темы",
        )
    }

    private fun CalendarRow.toRow(): UiState.Row = when (val topic = topic) {
        null -> UiState.Row.EmptyDay(date = ReleaseDateFormat.short(date))
        else -> topic.toRow(date)
    }

    private fun CalendarTopic.toRow(date: LocalDate): UiState.Row = UiState.Row.Topic(
        date = ReleaseDateFormat.short(date),
        ledBy = ledBy.text(),
        status = state.status(),
        steps = stepsText(),
        title = title,
        topicId = topicId,
    )

    private fun LedBy?.text(): String = when (this) {
        is LedBy.Cast -> name
        is LedBy.Guest -> "Гость"
        null -> ""
    }

    private fun ReleaseState.status(): Status = when (this) {
        ReleaseState.DRAFT -> Status(
            text = "Черновик",
            tone = Status.Tone.WARNING,
        )
        ReleaseState.PUBLISHED -> Status(
            text = "Опубликована",
            tone = Status.Tone.SUCCESS,
        )
        ReleaseState.TODAY -> Status(
            text = "Сегодня",
            tone = Status.Tone.SUCCESS,
        )
        ReleaseState.OUT -> Status(
            text = "Вышла",
            tone = Status.Tone.MUTED,
        )
    }

    private fun CalendarTopic.stepsText(): String = if (completeSteps == steps) "$steps" else "$completeSteps из $steps"

    private fun List<Todo>.toTodoList(): UiState.TodoList {
        if (isEmpty()) return UiState.TodoList.Empty(message = "Всё сделано")
        return UiState.TodoList.Entries(entries = map { todo -> todo.toEntry() })
    }

    private fun Todo.toEntry(): UiState.TodoEntry = when (this) {
        is Todo.Draft -> UiState.TodoEntry.Draft(
            date = ReleaseDateFormat.short(date),
            note = note(),
            title = title,
            topicId = topicId,
        )
        is Todo.EmptyDay -> UiState.TodoEntry.EmptyDay(
            date = ReleaseDateFormat.short(date),
            note = "В этот день пользователи увидят пустой день.",
            title = "Темы нет.",
        )
    }

    private fun Todo.Draft.note(): String = if (gaps.isEmpty()) "Черновик. Опубликуйте." else "Дописать ${TopicGapText(gaps)}."
}
