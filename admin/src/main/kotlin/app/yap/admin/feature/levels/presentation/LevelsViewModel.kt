package app.yap.admin.feature.levels.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.hasEveryLevel
import app.yap.admin.feature.levels.domain.usecase.GetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.SaveLevelsUseCase
import app.yap.admin.feature.models.domain.Models
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LevelsViewModel(
    private val getLevelsUseCase: GetLevelsUseCase,
    private val saveLevelsUseCase: SaveLevelsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, LevelsUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    fun onEvent(event: Event) = when (event) {
        is Event.FormChanged -> onFormChanged(event)
        is Event.LevelClicked -> dataState.update { state -> state.copy(chosen = event.code) }
        is Event.Opened -> onOpened()
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onFormChanged(event: Event.FormChanged) {
        if (dataState.value.isSaving) return
        dataState.update { state ->
            state.copy(
                forms = state.forms + (event.code to event.form),
                saveResult = null,
            )
        }
    }

    private fun onOpened() {
        if (dataState.value.isSaving) return
        load()
    }

    private fun onRetryClicked() {
        val state = dataState.value
        if (!state.isLoadFailed && state.levels?.hasEveryLevel() != false) return
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(isLoadFailed = false) }
        viewModelScope.launch {
            when (val result = getLevelsUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(isLoadFailed = true) }
                is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
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
            when (val result = saveLevelsUseCase(state.forms)) {
                is SaveLevelsResult.Failed -> onSaveFailed(result)
                is SaveLevelsResult.Invalid,
                is SaveLevelsResult.ModelUnavailable,
                is SaveLevelsResult.NoIdioms,
                is SaveLevelsResult.NoWordCount,
                is SaveLevelsResult.UnreadNumber -> endSave(result)
                is SaveLevelsResult.Saved -> onSaved(result)
            }
        }
    }

    private suspend fun onSaveFailed(result: SaveLevelsResult.Failed) {
        endSave(result)
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    private fun onSaved(result: SaveLevelsResult.Saved) {
        if (!result.levels.hasEveryLevel()) {
            dataState.update { state -> state.copy(saveResult = result) }
            load()
            return
        }
        dataState.update { state ->
            state
                .loaded(result.levels)
                .copy(saveResult = result)
        }
    }

    private fun endSave(result: SaveLevelsResult) {
        dataState.update { state ->
            state.copy(
                isSaving = false,
                saveResult = result,
            )
        }
    }

    private fun DataState.loaded(levels: List<Level>): DataState = copy(
        forms = levels.toInputs(),
        isSaving = false,
        levels = levels,
    )

    data class DataState(
        val chosen: LevelCode = LevelCode.B1,
        val forms: Map<LevelCode, LevelInput> = emptyMap(),
        val isLoadFailed: Boolean = false,
        val isModelsLoadFailed: Boolean = false,
        val isSaving: Boolean = false,
        val levels: List<Level>? = null,
        val models: Models? = null,
        val saveResult: SaveLevelsResult? = null,
    )

    sealed interface UiState {

        data class Content(
            val block: Block,
            val idioms: List<IdiomsOption>,
            val isModelChoiceEnabled: Boolean,
            val isRefreshShown: Boolean,
            val isSaveEnabled: Boolean,
            val modelError: String?,
            val models: List<ModelOption>,
            val rows: List<Row>,
            val saveStatus: Status?,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

        data class Block(
            val code: LevelCode,
            val form: LevelInput,
            val rating: String?,
            val title: String,
        )

        data class IdiomsOption(
            val idioms: Idioms,
            val label: String,
        )

        data class ModelOption(
            val label: String,
            val model: String?,
        )

        data class Row(
            val code: LevelCode,
            val form: LevelInput,
            val idioms: IdiomsOption?,
            val isChosen: Boolean,
            val isRatingFromShown: Boolean,
            val label: String,
            val maxWordsLabel: String,
            val model: ModelOption,
            val range: String?,
            val ratingFromLabel: String,
            val ratingNote: String?,
            val vocabularyLabel: String,
        )
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data class FormChanged(
            val code: LevelCode,
            val form: LevelInput,
        ) : Event

        data class LevelClicked(val code: LevelCode) : Event

        data object Opened : Event

        data object RetryClicked : Event

        data object SaveClicked : Event
    }
}

