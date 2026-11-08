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
        if (dataState.value.isSending) return
        load()
    }

    private fun onPromptClicked(promptKey: PromptKey) {
        val state = dataState.value
        if (state.isSending || state.promptLoad == Load.LOADING) return
        if (promptKey !in state.prompts) {
            loadPrompt(promptKey)
            return
        }
        dataState.update { current ->
            current.copy(
                chosen = promptKey,
                promptLoad = Load.LOADED,
                sendStatus = SendStatus.Idle,
            )
        }
    }

    private fun onRestoreClicked(number: Int) {
        val state = dataState.value
        if (state.isSending) return
        startSending(SendStatus.Restoring)
        viewModelScope.launch {
            when (val result = restorePromptUseCase(state.chosen, number)) {
                is RestorePromptResult.Failed -> onSendFailed("Не удалось вернуть версию. Попробуйте ещё раз.")
                is RestorePromptResult.Restored -> dataState.update { current ->
                    current.sent(
                        prompt = result.prompt,
                        sendStatus = SendStatus.Restored(number),
                    )
                }
            }
        }
    }

    private fun onRetryClicked() {
        val state = dataState.value
        if (state.load == Load.FAILED) load()
        if (state.promptLoad == Load.FAILED) loadPrompt(state.chosen)
    }

    private fun load() {
        dataState.update { state -> state.copy(load = Load.LOADING) }
        viewModelScope.launch {
            when (val result = getPromptsUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
                is LoadResult.Loaded -> {
                    dataState.update { state ->
                        state.copy(
                            load = Load.LOADED,
                            summaries = result.value,
                        )
                    }
                    loadPrompt(dataState.value.chosen)
                }
            }
        }
    }

    private fun loadPrompt(promptKey: PromptKey) {
        dataState.update { state ->
            state.copy(
                chosen = promptKey,
                promptLoad = Load.LOADING,
                sendStatus = SendStatus.Idle,
            )
        }
        viewModelScope.launch {
            when (val result = getPromptUseCase(promptKey)) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(promptLoad = Load.FAILED) }
                is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
            }
        }
    }

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSending || state.promptLoad != Load.LOADED) return
        startSending(SendStatus.Saving)
        viewModelScope.launch {
            when (val result = savePromptUseCase(state.chosen, state.text)) {
                is SavePromptResult.EmptyText -> dataState.update { current ->
                    current.copy(sendStatus = SendStatus.EmptyText)
                }
                is SavePromptResult.Failed -> onSendFailed("Не удалось сохранить. Попробуйте ещё раз.")
                is SavePromptResult.Saved -> dataState.update { current ->
                    current.sent(
                        prompt = result.prompt,
                        sendStatus = SendStatus.Saved,
                    )
                }
            }
        }
    }

    private fun startSending(sendStatus: SendStatus) {
        dataState.update { state -> state.copy(sendStatus = sendStatus) }
    }

    private suspend fun onSendFailed(message: String) {
        dataState.update { state -> state.copy(sendStatus = SendStatus.Idle) }
        newsChannel.send(News.ShowSnackbar(message))
    }

    private fun onTextChanged(text: String) {
        if (dataState.value.isSending) return
        dataState.update { state ->
            state.copy(
                sendStatus = SendStatus.Idle,
                texts = state.texts + (state.chosen to text),
            )
        }
    }

    data class DataState(
        val chosen: PromptKey = PromptKey.TOPIC_REPLY,
        val load: Load = Load.LOADING,
        val promptLoad: Load = Load.LOADING,
        val prompts: Map<PromptKey, Prompt> = emptyMap(),
        val sendStatus: SendStatus = SendStatus.Idle,
        val summaries: List<PromptSummary> = emptyList(),
        val texts: Map<PromptKey, String> = emptyMap(),
    ) {

        val isSending: Boolean
            get() = sendStatus is SendStatus.Restoring || sendStatus is SendStatus.Saving

        val text: String
            get() = texts[chosen] ?: prompts.getValue(chosen).text.orEmpty()

        fun loaded(prompt: Prompt): DataState = copy(
            promptLoad = Load.LOADED,
            prompts = prompts + (prompt.promptKey to prompt),
        )

        fun sent(prompt: Prompt, sendStatus: SendStatus): DataState = loaded(prompt).copy(
            sendStatus = sendStatus,
            texts = texts - prompt.promptKey,
        )
    }

    enum class Load {
        FAILED,
        LOADED,
        LOADING,
    }

    sealed interface SendStatus {

        data object EmptyText : SendStatus

        data object Idle : SendStatus

        data class Restored(val number: Int) : SendStatus

        data object Restoring : SendStatus

        data object Saved : SendStatus

        data object Saving : SendStatus
    }

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
