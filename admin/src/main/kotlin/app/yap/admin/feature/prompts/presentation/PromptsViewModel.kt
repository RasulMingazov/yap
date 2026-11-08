package app.yap.admin.feature.prompts.presentation

import androidx.lifecycle.ViewModel
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary

internal class PromptsViewModel : ViewModel() {

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
}
