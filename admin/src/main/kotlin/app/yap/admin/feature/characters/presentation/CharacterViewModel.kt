package app.yap.admin.feature.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.usecase.GetCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.SaveCharacterUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterViewModel(
    characterId: CharacterId?,
    private val getCharacterUseCase: GetCharacterUseCase,
    private val saveCharacterUseCase: SaveCharacterUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState(characterId = characterId))
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, CharacterUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    init {
        if (characterId != null) load(characterId)
    }

    fun onEvent(event: Event) = when (event) {
        is Event.FormChanged -> dataState.update { state ->
            state.copy(
                form = event.form,
                saveResult = null,
            )
        }
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onRetryClicked() {
        val state = dataState.value
        val characterId = state.characterId
        if (!state.isLoadFailed || characterId == null) return
        load(characterId)
    }

    private fun load(characterId: CharacterId) {
        dataState.update { state -> state.copy(isLoadFailed = false) }
        viewModelScope.launch {
            when (val result = getCharacterUseCase(characterId)) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(isLoadFailed = true) }
                is LoadResult.Loaded -> dataState.update { state -> state.saved(result.value.profile) }
            }
        }
    }

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSaving) return
        dataState.update { current ->
            current.copy(
                isSaving = true,
                saveResult = null,
            )
        }
        viewModelScope.launch {
            when (val result = saveCharacterUseCase(state.characterId, state.form)) {
                is SaveCharacterResult.Failed -> onSaveFailed(result)
                is SaveCharacterResult.Invalid -> dataState.update { current ->
                    current.copy(
                        isSaving = false,
                        saveResult = result,
                    )
                }
                is SaveCharacterResult.Saved -> dataState.update { current ->
                    current
                        .saved(result.character.profile)
                        .copy(
                            characterId = result.character.characterId,
                            saveResult = result,
                        )
                }
            }
        }
    }

    private suspend fun onSaveFailed(result: SaveCharacterResult.Failed) {
        dataState.update { state ->
            state.copy(
                isSaving = false,
                saveResult = result,
            )
        }
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    data class DataState(
        val characterId: CharacterId?,
        val form: CharacterProfile = EMPTY_PROFILE,
        val isLoadFailed: Boolean = false,
        val isSaving: Boolean = false,
        val saveResult: SaveCharacterResult? = null,
        val savedProfile: CharacterProfile? = null,
    ) {

        fun saved(profile: CharacterProfile): DataState = copy(
            form = profile,
            isSaving = false,
            savedProfile = profile,
        )
    }

    sealed interface UiState {

        data class Content(
            val form: CharacterProfile,
            val hint: String?,
            val isSaveEnabled: Boolean,
            val saveStatus: Status?,
            val subtitle: String,
            val title: String,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data class FormChanged(val form: CharacterProfile) : Event

        data object RetryClicked : Event

        data object SaveClicked : Event
    }

    private companion object {

        val EMPTY_PROFILE = CharacterProfile(
            history = "",
            manner = "",
            name = "",
            personality = "",
        )
    }
}
