package app.yap.admin.feature.topics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.usecase.GetTopicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TopicsViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, TopicsUiStateMapper)

    fun onEvent(event: Event) = when (event) {
        is Event.Opened -> onOpened()
        is Event.RetryClicked -> onRetryClicked()
    }

    private fun onOpened() {
        val state = dataState.value
        if (state.isLoading) return
        if (state.topics !is LoadTopicsResult.Loaded) {
            dataState.update { current -> current.copy(topics = null) }
        }
        load()
    }

    private fun onRetryClicked() {
        if (dataState.value.topics !is LoadTopicsResult.Failed) return
        dataState.update { state -> state.copy(topics = null) }
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            val topics = getTopicsUseCase()
            dataState.update { state -> state.loaded(topics) }
        }
    }

    data class DataState(
        val isLoading: Boolean = false,
        val topics: LoadTopicsResult? = null,
    ) {

        fun loaded(result: LoadTopicsResult): DataState = copy(
            isLoading = false,
            topics = if (result is LoadTopicsResult.Failed && topics is LoadTopicsResult.Loaded) topics else result,
        )
    }

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
