package app.yap.admin.feature.topics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.usecase.GetTopicCalendarUseCase
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TopicsViewModel(
    private val getTopicCalendarUseCase: GetTopicCalendarUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, TopicsUiStateMapper)

    fun onEvent(event: Event) = when (event) {
        is Event.Opened -> load()
        is Event.RetryClicked -> load()
    }

    private fun load() {
        if (dataState.value.isLoading) return
        dataState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getTopicCalendarUseCase()) {
                is LoadResult.Failed -> dataState.update { state ->
                    state.copy(
                        isLoadFailed = true,
                        isLoading = false,
                    )
                }
                is LoadResult.Loaded -> dataState.update { state ->
                    state.copy(
                        calendar = result.value,
                        isLoadFailed = false,
                        isLoading = false,
                    )
                }
            }
        }
    }

    data class DataState(
        val calendar: TopicCalendar? = null,
        val isLoadFailed: Boolean = false,
        val isLoading: Boolean = false,
    )

    sealed interface UiState {

        data class Content(
            val reloadError: ReloadError?,
            val rows: List<Row>,
            val todo: TodoList,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

        data class ReloadError(
            val isRetrying: Boolean,
            val message: String,
        )

        sealed interface Row {

            data class EmptyDay(
                val date: String,
                val releaseDate: LocalDate,
            ) : Row

            data class Topic(
                val date: String,
                val ledBy: String,
                val status: Status,
                val steps: String,
                val title: String,
                val topicId: TopicId,
            ) : Row
        }

        sealed interface TodoEntry {

            data class Draft(
                val date: String,
                val note: String,
                val title: String,
                val topicId: TopicId,
            ) : TodoEntry

            data class EmptyDay(
                val date: String,
                val note: String,
                val releaseDate: LocalDate,
                val title: String,
            ) : TodoEntry
        }

        sealed interface TodoList {

            data class Empty(val message: String) : TodoList

            data class Entries(val entries: List<TodoEntry>) : TodoList
        }
    }

    sealed interface Event {

        data object Opened : Event

        data object RetryClicked : Event
    }
}
