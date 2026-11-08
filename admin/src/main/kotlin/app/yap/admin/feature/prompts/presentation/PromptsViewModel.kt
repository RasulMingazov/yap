package app.yap.admin.feature.prompts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.usecase.GetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.GetPromptsUseCase
import app.yap.admin.feature.prompts.domain.usecase.RestorePromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.SavePromptUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PromptsViewModel(
    private val getPromptUseCase: GetPromptUseCase,
    private val getPromptsUseCase: GetPromptsUseCase,
    private val restorePromptUseCase: RestorePromptUseCase,
    private val savePromptUseCase: SavePromptUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, PromptsUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    fun onEvent(event: Event) = when (event) {
        is Event.Opened -> onOpened()
        is Event.PromptClicked -> onPromptClicked(event.promptKey)
        is Event.RestoreClicked -> onRestoreClicked(event.number)
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
        is Event.TextChanged -> onTextChanged(event.text)
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onOpened() {
        if (isSending()) return
        load()
    }

    private fun onPromptClicked(promptKey: PromptKey) {
        val state = dataState.value
        if (isSending() || state.isPromptLoading) return
        if (promptKey !in state.prompts) {
            loadPrompt(promptKey)
            return
        }
        dataState.update { current ->
            current.copy(
                chosen = promptKey,
                isPromptLoadFailed = false,
                restoredNumber = null,
                saveResult = null,
            )
        }
    }

    private fun onRestoreClicked(number: Int) {
        if (isSending()) return
        val promptKey = dataState.value.chosen
        dataState.update { state ->
            state.copy(
                isRestoring = true,
                restoredNumber = null,
                saveResult = null,
            )
        }
        viewModelScope.launch {
            when (val result = restorePromptUseCase(promptKey, number)) {
                is RestorePromptResult.Failed -> onRestoreFailed()
                is RestorePromptResult.Restored -> dataState.update { state ->
                    state
                        .sent(result.prompt)
                        .copy(restoredNumber = number)
                }
            }
        }
    }

    private suspend fun onRestoreFailed() {
        dataState.update { state -> state.copy(isRestoring = false) }
        newsChannel.send(News.ShowSnackbar("Не удалось вернуть версию. Попробуйте ещё раз."))
    }

    private fun onRetryClicked() {
        val state = dataState.value
        if (state.isListLoadFailed) load()
        if (state.isPromptLoadFailed) loadPrompt(state.chosen)
    }

    private fun load() {
        dataState.update { state ->
            state.copy(
                isListLoadFailed = false,
                isListLoading = true,
            )
        }
        viewModelScope.launch {
            when (val result = getPromptsUseCase()) {
                is LoadResult.Failed -> dataState.update { state ->
                    state.copy(
                        isListLoadFailed = true,
                        isListLoading = false,
                    )
                }
                is LoadResult.Loaded -> {
                    loadPrompt(dataState.value.chosen)
                    dataState.update { state ->
                        state.copy(
                            isListLoading = false,
                            summaries = result.value,
                        )
                    }
                }
            }
        }
    }

    private fun loadPrompt(promptKey: PromptKey) {
        dataState.update { state ->
            state.copy(
                chosen = promptKey,
                isPromptLoadFailed = false,
                isPromptLoading = true,
                restoredNumber = null,
                saveResult = null,
            )
        }
        viewModelScope.launch {
            when (val result = getPromptUseCase(promptKey)) {
                is LoadResult.Failed -> dataState.update { state ->
                    state.copy(
                        isPromptLoadFailed = true,
                        isPromptLoading = false,
                    )
                }
                is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
            }
        }
    }

    private fun DataState.loaded(prompt: Prompt): DataState = copy(
        isPromptLoading = false,
        prompts = prompts + (prompt.promptKey to prompt),
        texts = mapOf(prompt.promptKey to prompt.text.orEmpty()) + texts,
    )

    private fun onSaveClicked() {
        val state = dataState.value
        if (isSending() || state.isPromptLoading || state.isPromptLoadFailed) return
        dataState.update { current ->
            current.copy(
                isSaving = true,
                restoredNumber = null,
                saveResult = null,
            )
        }
        viewModelScope.launch {
            when (val result = savePromptUseCase(state.chosen, state.texts.getValue(state.chosen))) {
                is SavePromptResult.Failed -> onSaveFailed(result)
                is SavePromptResult.Rejected -> endSave(result)
                is SavePromptResult.Saved -> dataState.update { current ->
                    current
                        .sent(result.prompt)
                        .copy(saveResult = result)
                }
            }
        }
    }

    private suspend fun onSaveFailed(result: SavePromptResult.Failed) {
        endSave(result)
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    private fun endSave(result: SavePromptResult) {
        dataState.update { state ->
            state.copy(
                isSaving = false,
                saveResult = result,
            )
        }
    }

    private fun DataState.sent(prompt: Prompt): DataState = copy(
        isRestoring = false,
        isSaving = false,
        prompts = prompts + (prompt.promptKey to prompt),
        texts = texts + (prompt.promptKey to prompt.text.orEmpty()),
    )

    private fun onTextChanged(text: String) {
        if (isSending()) return
        dataState.update { state ->
            state.copy(
                restoredNumber = null,
                saveResult = null,
                texts = state.texts + (state.chosen to text),
            )
        }
    }

    private fun isSending(): Boolean {
        val state = dataState.value
        return state.isRestoring || state.isSaving
    }

    data class DataState(
        val chosen: PromptKey = PromptKey.TOPIC_REPLY,
        val isListLoadFailed: Boolean = false,
        val isListLoading: Boolean = false,
        val isPromptLoadFailed: Boolean = false,
        val isPromptLoading: Boolean = false,
        val isRestoring: Boolean = false,
        val isSaving: Boolean = false,
        val prompts: Map<PromptKey, Prompt> = emptyMap(),
        val restoredNumber: Int? = null,
        val saveResult: SavePromptResult? = null,
        val summaries: List<PromptSummary>? = null,
        val texts: Map<PromptKey, String> = emptyMap(),
    )

    sealed interface UiState {

        data class Content(
            val editor: Editor,
            val groups: List<Group>,
            val isListEnabled: Boolean,
            val isSaveEnabled: Boolean,
            val saveStatus: Status,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

        sealed interface Editor {

            data class Error(val message: String) : Editor

            data class Loaded(
                val isEditable: Boolean,
                val isRestoreEnabled: Boolean,
                val isTextError: Boolean,
                val state: String,
                val text: String,
                val title: String,
                val variables: List<String>,
                val versions: List<Version>,
                val versionsEmptyMessage: String?,
            ) : Editor

            data object Loading : Editor
        }

        data class Group(
            val items: List<Item>,
            val title: String,
        )

        data class Item(
            val isChosen: Boolean,
            val promptKey: PromptKey,
            val title: String,
            val usage: String,
        )

        data class Version(
            val isRestoreShown: Boolean,
            val label: String,
            val number: Int,
            val savedAt: String,
            val state: Status,
        )
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data object Opened : Event

        data class PromptClicked(val promptKey: PromptKey) : Event

        data class RestoreClicked(val number: Int) : Event

        data object RetryClicked : Event

        data object SaveClicked : Event

        data class TextChanged(val text: String) : Event
    }
}
