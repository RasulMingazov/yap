package app.yap.admin.feature.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.usecase.GetCharactersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharactersViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, CharactersUiStateMapper)

    fun onEvent(event: Event) = when (event) {
        is Event.Opened -> onOpened()
        is Event.RetryClicked -> onRetryClicked()
    }

    private fun onOpened() {
        if (dataState.value.isLoading) return
        load()
    }

    private fun onRetryClicked() {
        val state = dataState.value
        if (!state.isLoadFailed || state.characters != null) return
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
            when (val result = getCharactersUseCase()) {
                is LoadResult.Failed -> dataState.update { state ->
                    state.copy(
                        isLoadFailed = true,
                        isLoading = false,
                    )
                }
                is LoadResult.Loaded -> dataState.update { state ->
                    state.copy(
                        characters = result.value,
                        isLoading = false,
                    )
                }
            }
        }
    }

    data class DataState(
        val characters: List<Character>? = null,
        val isLoadFailed: Boolean = false,
        val isLoading: Boolean = false,
    )

    sealed interface UiState {

        data class Content(val rows: List<Row>) : UiState {

            data class Row(
                val characterId: CharacterId,
                val name: String,
                val personality: String,
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
