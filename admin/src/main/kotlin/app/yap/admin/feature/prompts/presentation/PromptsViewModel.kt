package app.yap.admin.feature.prompts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.usecase.GetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.GetPromptsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PromptsViewModel(
    private val getPromptUseCase: GetPromptUseCase,
    private val getPromptsUseCase: GetPromptsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, PromptsUiStateMapper::map)

    init {
        load()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.PromptClicked -> onPromptClicked(event.promptKey)
        is Event.RetryClicked -> onRetryClicked()
        is Event.TextChanged -> onTextChanged(event.text)
    }

    private fun onPromptClicked(promptKey: PromptKey) {
        val state = dataState.value
        if (state.load == Load.LOADING) return
        val isLoaded = promptKey in state.prompts
        dataState.update { state ->
            state.copy(
                chosen = promptKey,
                load = if (isLoaded) Load.LOADED else Load.LOADING,
            )
        }
        if (isLoaded) return
        viewModelScope.launch { loadPrompt(promptKey) }
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

    private fun onTextChanged(text: String) {
        dataState.update { state -> state.copy(texts = state.texts + (state.chosen to text)) }
    }

    data class DataState(
        val chosen: PromptKey = PromptKey.TOPIC_REPLY,
        val isSaving: Boolean = false,
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

    sealed interface Event {

        data class PromptClicked(val promptKey: PromptKey) : Event

        data object RetryClicked : Event

        data class TextChanged(val text: String) : Event
    }
}
