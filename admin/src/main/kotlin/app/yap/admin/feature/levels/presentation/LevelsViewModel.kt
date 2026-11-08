package app.yap.admin.feature.levels.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.LevelSettings
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.hasEveryLevel
import app.yap.admin.feature.levels.domain.usecase.GetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.SaveLevelsUseCase
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
                saveStatus = null,
            )
        }
    }

    private fun onOpened() {
        if (dataState.value.isSaving) return
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
                levels = null,
            )
        }
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
        val checks = state.forms.map { (code, form) -> check(code, form) }
        val problem = checks
            .filterIsInstance<FormCheck.Problem>()
            .firstOrNull()
        if (problem != null) {
            dataState.update { current -> current.copy(saveStatus = problem.status) }
            return
        }
        save(checks.filterIsInstance<FormCheck.Ready>().map { ready -> ready.entry })
    }

    private fun check(code: LevelCode, form: LevelInput): FormCheck {
        val idioms = form.idioms
        val maxWords = form.maxWords.toIntOrNull()
        return when {
            form.ratingFrom.isNotBlank() && form.ratingFrom.toIntOrNull() == null ->
                FormCheck.Problem(SaveStatus.UnreadNumber(code, NumberField.RATING_FROM))
            maxWords == null -> FormCheck.Problem(SaveStatus.UnreadNumber(code, NumberField.MAX_WORDS))
            idioms == null -> FormCheck.Problem(SaveStatus.NoIdioms(code))
            else -> FormCheck.Ready(
                LevelEntry(
                    code = code,
                    settings = LevelSettings(
                        block = form.block,
                        idioms = idioms,
                        maxWords = maxWords,
                        ratingFrom = form.ratingFrom.toIntOrNull(),
                        vocabulary = form.vocabulary,
                    ),
                ),
            )
        }
    }

    private fun save(entries: List<LevelEntry>) {
        dataState.update { state ->
            state.copy(
                isSaving = true,
                saveStatus = null,
            )
        }
        viewModelScope.launch {
            when (val result = saveLevelsUseCase(entries)) {
                is SaveLevelsResult.Failed -> onSaveFailed()
                is SaveLevelsResult.Invalid -> dataState.update { state ->
                    state.copy(
                        isSaving = false,
                        saveStatus = SaveStatus.Invalid,
                    )
                }
                is SaveLevelsResult.Saved -> onSaved(result.levels)
            }
        }
    }

    private fun onSaved(levels: List<Level>) {
        if (!levels.hasEveryLevel()) {
            dataState.update { state -> state.copy(saveStatus = SaveStatus.Saved) }
            load()
            return
        }
        dataState.update { state ->
            state
                .loaded(levels)
                .copy(saveStatus = SaveStatus.Saved)
        }
    }

    private fun DataState.loaded(levels: List<Level>): DataState = copy(
        forms = levels.toInputs(),
        isSaving = false,
        levels = levels,
    )

    private suspend fun onSaveFailed() {
        dataState.update { state -> state.copy(isSaving = false) }
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    data class DataState(
        val chosen: LevelCode = LevelCode.B1,
        val forms: Map<LevelCode, LevelInput> = emptyMap(),
        val isLoadFailed: Boolean = false,
        val isSaving: Boolean = false,
        val levels: List<Level>? = null,
        val saveStatus: SaveStatus? = null,
    )

    sealed interface FormCheck {

        data class Problem(val status: SaveStatus) : FormCheck

        data class Ready(val entry: LevelEntry) : FormCheck
    }

    enum class NumberField {
        MAX_WORDS,
        RATING_FROM,
    }

    sealed interface SaveStatus {

        data object Invalid : SaveStatus

        data class NoIdioms(val code: LevelCode) : SaveStatus

        data object Saved : SaveStatus

        data class UnreadNumber(
            val code: LevelCode,
            val field: NumberField,
        ) : SaveStatus
    }

    sealed interface UiState {

        data class Content(
            val block: Block,
            val idioms: List<IdiomsOption>,
            val isSaveEnabled: Boolean,
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

        data class Row(
            val code: LevelCode,
            val form: LevelInput,
            val idioms: IdiomsOption?,
            val isChosen: Boolean,
            val isRatingFromShown: Boolean,
            val label: String,
            val maxWordsLabel: String,
            val range: String?,
            val ratingFromLabel: String,
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

