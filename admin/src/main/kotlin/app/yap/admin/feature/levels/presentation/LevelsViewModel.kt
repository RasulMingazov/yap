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
import app.yap.admin.feature.levels.domain.LevelSettings
import app.yap.admin.feature.levels.domain.SaveLevelsResult
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
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, LevelsUiStateMapper::map)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.FormChanged -> dataState.update { state ->
            state.copy(
                forms = state.forms + (event.code to event.form),
                saveStatus = null,
            )
        }
        is Event.LevelClicked -> dataState.update { state -> state.copy(chosen = event.code) }
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onRetryClicked() {
        if (dataState.value.load != Load.FAILED) return
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(load = Load.LOADING) }
        viewModelScope.launch {
            when (val result = getLevelsUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
                is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
            }
        }
    }

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSaving) return
        val entries = state.forms.mapNotNull { (code, form) -> form.toEntry(code) }
        val unreadCode = state.forms.keys.firstOrNull { code -> entries.none { entry -> entry.code == code } }
        if (unreadCode == null) {
            save(entries)
        } else {
            dataState.update { current -> current.copy(saveStatus = SaveStatus.UnreadNumber(unreadCode)) }
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
                is SaveLevelsResult.Saved -> dataState.update { state ->
                    state
                        .loaded(result.levels)
                        .copy(saveStatus = SaveStatus.Saved)
                }
            }
        }
    }

    private suspend fun onSaveFailed() {
        dataState.update { state -> state.copy(isSaving = false) }
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    data class DataState(
        val chosen: LevelCode = LevelCode.B1,
        val forms: Map<LevelCode, LevelForm> = emptyMap(),
        val isSaving: Boolean = false,
        val levels: List<Level> = emptyList(),
        val load: Load = Load.LOADING,
        val saveStatus: SaveStatus? = null,
    ) {

        fun loaded(levels: List<Level>): DataState = copy(
            forms = levels.associate { level -> level.code to (level.settings?.let(LevelForm::of) ?: LevelForm.EMPTY) },
            isSaving = false,
            levels = levels,
            load = Load.LOADED,
        )
    }

    data class LevelForm(
        val block: String,
        val idioms: Idioms,
        val maxWords: String,
        val ratingFrom: String,
        val vocabulary: String,
    ) {

        fun toEntry(code: LevelCode): LevelEntry? {
            val readMaxWords = maxWords.toIntOrNull()
            val readRatingFrom = ratingFrom.toIntOrNull()
            if (readMaxWords == null || readRatingFrom == null && ratingFrom.isNotBlank()) return null
            return LevelEntry(
                code = code,
                settings = LevelSettings(
                    block = block,
                    idioms = idioms,
                    maxWords = readMaxWords,
                    ratingFrom = readRatingFrom,
                    vocabulary = vocabulary,
                ),
            )
        }

        companion object {

            val EMPTY = LevelForm(
                block = "",
                idioms = Idioms.NEVER,
                maxWords = "",
                ratingFrom = "",
                vocabulary = "",
            )

            fun of(settings: LevelSettings): LevelForm = LevelForm(
                block = settings.block,
                idioms = settings.idioms,
                maxWords = settings.maxWords.toString(),
                ratingFrom = settings.ratingFrom?.toString().orEmpty(),
                vocabulary = settings.vocabulary,
            )
        }
    }

    enum class Load {
        FAILED,
        LOADED,
        LOADING,
    }

    sealed interface SaveStatus {

        data object Invalid : SaveStatus

        data object Saved : SaveStatus

        data class UnreadNumber(val code: LevelCode) : SaveStatus
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
            val rating: String?,
            val text: String,
            val title: String,
        )

        data class IdiomsOption(
            val idioms: Idioms,
            val label: String,
        )

        data class Row(
            val code: LevelCode,
            val form: LevelForm,
            val isChosen: Boolean,
            val label: String,
            val range: String?,
        )
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data class FormChanged(
            val code: LevelCode,
            val form: LevelForm,
        ) : Event

        data class LevelClicked(val code: LevelCode) : Event

        data object RetryClicked : Event

        data object SaveClicked : Event
    }
}
