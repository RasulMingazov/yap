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
import app.yap.admin.feature.models.domain.TaskModel
import app.yap.admin.feature.models.domain.usecase.GetModelsUseCase
import app.yap.admin.feature.models.domain.usecase.SaveModelsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ModelsViewModel(
    private val getModelsUseCase: GetModelsUseCase,
    private val saveModelsUseCase: SaveModelsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, ModelsUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    fun onEvent(event: Event) = when (event) {
        is Event.ModelChanged -> onModelChanged(event)
        is Event.Opened -> onOpened()
        is Event.ProviderChanged -> onProviderChanged(event)
        is Event.RefreshClicked -> onRefreshClicked()
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onModelChanged(event: Event.ModelChanged) {
        val choice = dataState.value.choices.getValue(event.task)
        changeChoice(
            choice = choice.copy(model = event.model),
            task = event.task,
        )
    }

    private fun onOpened() {
        if (dataState.value.isSaving) return
        load()
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
        if (dataState.value.isSaving) return
        dataState.update { state ->
            state.copy(
                choices = state.choices + (task to choice),
                saveResult = null,
            )
        }
    }

    private fun onRefreshClicked() {
        dataState.update { state -> state.copy(saveResult = null) }
        load()
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

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSaving) return
        dataState.update { current ->
            current.copy(
                isSaving = true,
                saveResult = null,
            )
        }
        val tasks = state.choices.map { (task, choice) ->
            TaskModel(
                choice = choice,
                task = task,
            )
        }
        viewModelScope.launch {
            when (val result = saveModelsUseCase(tasks)) {
                is SaveModelsResult.CatalogChanged,
                is SaveModelsResult.Invalid -> endSave(result)
                is SaveModelsResult.Failed -> onSaveFailed(result)
                is SaveModelsResult.Saved -> dataState.update { current ->
                    current
                        .loaded(result.models)
                        .copy(saveResult = result)
                }
            }
        }
    }

    private suspend fun onSaveFailed(result: SaveModelsResult.Failed) {
        endSave(result)
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    private fun endSave(result: SaveModelsResult) {
        dataState.update { state ->
            state.copy(
                isSaving = false,
                saveResult = result,
            )
        }
    }

    private fun DataState.loaded(models: Models): DataState = copy(
        choices = models.tasks.associate { taskModel ->
            taskModel.task to taskModel.choice
        },
        isSaving = false,
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

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
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

        data object RefreshClicked : Event

        data object RetryClicked : Event

        data object SaveClicked : Event
    }
}
