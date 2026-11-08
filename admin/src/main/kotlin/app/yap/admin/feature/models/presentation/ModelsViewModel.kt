package app.yap.admin.feature.models.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.models.domain.ModelChoice
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.usecase.GetModelsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ModelsViewModel(
    private val getModelsUseCase: GetModelsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, ModelsUiStateMapper)

    fun onEvent(event: Event) = when (event) {
        is Event.ModelChanged -> onModelChanged(event)
        is Event.Opened -> load()
        is Event.ProviderChanged -> onProviderChanged(event)
        is Event.RetryClicked -> onRetryClicked()
    }

    private fun onModelChanged(event: Event.ModelChanged) {
        val choice = dataState.value.choices.getValue(event.task)
        changeChoice(
            choice = choice.copy(model = event.model),
            task = event.task,
        )
    }

    private fun onProviderChanged(event: Event.ProviderChanged) {
        if (dataState.value.choices.getValue(event.task).provider == event.provider) return
        val provider = checkNotNull(dataState.value.models)
            .providers
            .first { provider -> provider.code == event.provider }
        changeChoice(
            choice = ModelChoice(
                model = provider.models.first(),
                provider = provider.code,
            ),
            task = event.task,
        )
    }

    private fun changeChoice(
        choice: ModelChoice,
        task: ModelTask,
    ) {
        dataState.update { state -> state.copy(choices = state.choices + (task to choice)) }
    }

    private fun onRetryClicked() {
        val state = dataState.value
        if (!state.isLoadFailed && state.models?.hasEveryTask() != false) return
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(isLoadFailed = false) }
        viewModelScope.launch {
            when (val result = getModelsUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(isLoadFailed = true) }
                is LoadResult.Loaded -> dataState.update { state -> state.loaded(result.value) }
            }
        }
    }

    private fun DataState.loaded(models: Models): DataState = copy(
        choices = models.tasks.associate { taskModel ->
            taskModel.task to taskModel.choice
        },
        models = models,
    )

    data class DataState(
        val choices: Map<ModelTask, ModelChoice> = emptyMap(),
        val isLoadFailed: Boolean = false,
        val isSaving: Boolean = false,
        val models: Models? = null,
        val saveResult: SaveModelsResult? = null,
    )

    sealed interface UiState {

        data class Content(
            val isFormEnabled: Boolean,
            val isRefreshShown: Boolean,
            val providerOptions: List<ProviderOption>,
            val providers: List<ProviderRow>,
            val saveStatus: Status?,
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

    sealed interface Event {

        data class ModelChanged(
            val model: String,
            val task: ModelTask,
        ) : Event

        data object Opened : Event

        data class ProviderChanged(
            val provider: String,
            val task: ModelTask,
        ) : Event

        data object RetryClicked : Event
    }
}
