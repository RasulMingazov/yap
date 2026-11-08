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
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, PromptsUiStateMapper::map)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: Event) = when (event) {
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

    private fun onPromptClicked(promptKey: PromptKey) {
        val state = dataState.value
        if (state.isSending || state.load == Load.LOADING) return
        val isLoaded = promptKey in state.prompts
        dataState.update { current ->
            current.copy(
                chosen = promptKey,
                load = if (isLoaded) Load.LOADED else Load.LOADING,
                saveStatus = null,
            )
        }
        if (isLoaded) return
        viewModelScope.launch { loadPrompt(promptKey) }
    }

    private fun onRestoreClicked(number: Int) {
        val state = dataState.value
        if (state.isSending) return
        startSending()
        viewModelScope.launch {
            when (val result = restorePromptUseCase(state.chosen, number)) {
                is RestorePromptResult.Failed -> onSendFailed("Не удалось вернуть версию. Попробуйте ещё раз.")
                is RestorePromptResult.Restored -> dataState.update { current -> current.sent(result.prompt) }
            }
        }
    }

    private fun onRetryClicked() {
        if (dataState.value.load != Load.FAILED) return
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(load = Load.LOADING) }
        viewModelScope.launch {
            when (val result = getPromptsUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
                is LoadResult.Loaded -> {
                    dataState.update { state -> state.copy(summaries = result.value) }
                    loadPrompt(dataState.value.chosen)
                }
            }
        }
    }

    private suspend fun loadPrompt(promptKey: PromptKey) {
        when (val result = getPromptUseCase(promptKey)) {
            is LoadResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
            is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
        }
    }

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSending) return
        startSending()
        viewModelScope.launch {
            when (val result = savePromptUseCase(state.chosen, state.text)) {
                is SavePromptResult.EmptyText -> dataState.update { current ->
                    current.copy(
                        isSending = false,
                        saveStatus = SaveStatus.EMPTY_TEXT,
                    )
                }
                is SavePromptResult.Failed -> onSendFailed("Не удалось сохранить. Попробуйте ещё раз.")
                is SavePromptResult.Saved -> dataState.update { current ->
                    current
                        .sent(result.prompt)
                        .copy(saveStatus = SaveStatus.SAVED)
                }
            }
        }
    }

    private fun startSending() {
        dataState.update { state ->
            state.copy(
                isSending = true,
                saveStatus = null,
            )
        }
    }

    private suspend fun onSendFailed(message: String) {
        dataState.update { state -> state.copy(isSending = false) }
        newsChannel.send(News.ShowSnackbar(message))
    }

    private fun onTextChanged(text: String) {
        if (dataState.value.isSending) return
        dataState.update { state ->
            state.copy(
                saveStatus = null,
                texts = state.texts + (state.chosen to text),
            )
        }
    }

    data class DataState(
        val chosen: PromptKey = PromptKey.TOPIC_REPLY,
        val isSending: Boolean = false,
        val load: Load = Load.LOADING,
        val prompts: Map<PromptKey, Prompt> = emptyMap(),
        val saveStatus: SaveStatus? = null,
        val summaries: List<PromptSummary> = emptyList(),
        val texts: Map<PromptKey, String> = emptyMap(),
    ) {

        val text: String
            get() = texts[chosen] ?: prompts.getValue(chosen).text.orEmpty()

        fun loaded(prompt: Prompt): DataState = copy(
            load = Load.LOADED,
            prompts = prompts + (prompt.promptKey to prompt),
        )

        fun sent(prompt: Prompt): DataState = loaded(prompt).copy(
            isSending = false,
            texts = texts - prompt.promptKey,
        )
    }

    enum class Load {
        FAILED,
        LOADED,
        LOADING,
    }

    enum class SaveStatus {
        EMPTY_TEXT,
        SAVED,
    }

    sealed interface UiState {

        data class Content(
            val groups: List<Group>,
            val isSaveEnabled: Boolean,
            val saveStatus: Status?,
            val state: String,
            val text: String,
            val title: String,
            val variables: List<String>,
            val versions: List<Version>,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

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
            val isCurrent: Boolean,
            val label: String,
            val number: Int,
            val savedAt: String,
            val state: String,
        )
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data class PromptClicked(val promptKey: PromptKey) : Event

        data class RestoreClicked(val number: Int) : Event

        data object RetryClicked : Event

        data object SaveClicked : Event

        data class TextChanged(val text: String) : Event
    }
}
