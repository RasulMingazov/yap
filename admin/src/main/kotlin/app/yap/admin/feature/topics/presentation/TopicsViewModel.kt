package app.yap.admin.feature.topics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.usecase.GetTopicCalendarUseCase
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
        is Event.Opened -> onOpened()
        is Event.RetryClicked -> onRetryClicked()
    }

    private fun onOpened() {
        if (dataState.value.isLoading) return
        load()
    }

    private fun onRetryClicked() {
        if (!dataState.value.isLoadFailed) return
        load()
    }

    private fun load() {
        dataState.update { state ->
            state.copy(
                isLoadFailed = false,
                isLoading = true,
            )
        }
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

        data class Content(val rows: List<Row>) : UiState {

            data class Row(
                val date: String,
                val title: String,
                val topicId: TopicId,
            )
        }

        data class Empty(val message: String) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState
    }

    sealed interface Event {

        data object Opened : Event

        data object RetryClicked : Event
    }
}
