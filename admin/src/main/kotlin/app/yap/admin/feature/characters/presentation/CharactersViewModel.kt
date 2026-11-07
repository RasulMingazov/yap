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
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, CharactersUiStateMapper::map)

    fun onEvent(event: Event) = when (event) {
        is Event.Opened -> onOpened()
        is Event.RetryClicked -> onRetryClicked()
    }

    private fun onOpened() {
        val state = dataState.value
        if (state.isLoading) return
        if (state.characters !is LoadResult.Loaded) {
            dataState.update { current -> current.copy(characters = null) }
        }
        load()
    }

    private fun onRetryClicked() {
        if (dataState.value.characters !is LoadResult.Failed) return
        dataState.update { state -> state.copy(characters = null) }
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch {
            val characters = getCharactersUseCase()
            dataState.update { state -> state.loaded(characters) }
        }
    }

    data class DataState(
        val characters: LoadResult<List<Character>>? = null,
        val isLoading: Boolean = false,
    ) {

        fun loaded(result: LoadResult<List<Character>>): DataState = copy(
            characters = if (result is LoadResult.Failed && characters is LoadResult.Loaded) characters else result,
            isLoading = false,
        )
    }

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
