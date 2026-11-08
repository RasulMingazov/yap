package app.yap.admin.feature.models.presentation

import androidx.lifecycle.ViewModel
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.models.domain.ModelChoice
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models

internal class ModelsViewModel : ViewModel() {

    data class DataState(
        val choices: Map<ModelTask, ModelChoice> = emptyMap(),
        val isLoadFailed: Boolean = false,
        val models: Models? = null,
    )

    sealed interface UiState {

        data class Content(
            val providerOptions: List<ProviderOption>,
            val providers: List<ProviderRow>,
            val tasks: List<TaskRow>,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

        data class ProviderOption(
            val code: String,
            val name: String,
        )

        data class ProviderRow(
            val models: String,
            val name: String,
            val state: Status,
            val step: String?,
        )

        data class TaskRow(
            val mark: Status?,
            val model: String,
            val models: List<String>,
            val provider: ProviderOption,
            val task: ModelTask,
            val title: String,
            val usage: String,
        )
    }
}
