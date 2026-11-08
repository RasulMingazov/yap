package app.yap.admin.feature.models.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.Provider
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.presentation.ModelsViewModel.DataState
import app.yap.admin.feature.models.presentation.ModelsViewModel.UiState

internal object ModelsUiStateMapper : (DataState) -> UiState {

    private const val LOAD_ERROR = "Не удалось загрузить модели"

    private val CONNECTED = Status(
        text = "Подключён",
        tone = Status.Tone.SUCCESS,
    )

    private val NOT_CONNECTED = Status(
        text = "Не подключён",
        tone = Status.Tone.WARNING,
    )

    private val SAVING = Status(
        text = "Сохранение…",
        tone = Status.Tone.MUTED,
    )

    override fun invoke(state: DataState): UiState {
        val models = state.models
        return when {
            state.isLoadFailed -> UiState.Error(message = LOAD_ERROR)
            models == null -> UiState.Loading
            !models.hasEveryTask() -> UiState.Error(message = LOAD_ERROR)
            else -> state.toContent(models)
        }
    }

    private fun DataState.toContent(models: Models): UiState.Content = UiState.Content(
        isFormEnabled = !isSaving,
        isRefreshShown = saveResult is SaveModelsResult.CatalogChanged,
        providerOptions = models.providers.map { provider -> provider.option() },
        providers = models.providers.map { provider -> provider.row() },
        saveStatus = saveStatus(),
        tasks = ModelTask.entries.map { task -> taskRow(providers = models.providers, task = task) },
    )

    private fun Provider.row(): UiState.ProviderRow = UiState.ProviderRow(
        models = models.joinToString(separator = ", "),
        name = name,
        state = connection(),
        step = step(),
    )

    private fun Provider.connection(): Status = if (connected) CONNECTED else NOT_CONNECTED

    private fun Provider.step(): String? = if (connected) null else "Добавьте ключ $keySetting в настройки сервера"

    private fun DataState.saveStatus(): Status? = if (isSaving) SAVING else saveResult?.status()

    private fun SaveModelsResult.status(): Status? = when (this) {
        is SaveModelsResult.CatalogChanged -> Status(
            text = "Список моделей на сервере изменился",
            tone = Status.Tone.ERROR,
        )
        is SaveModelsResult.Failed -> null
        is SaveModelsResult.Invalid -> Status(
            text = "Сервер не принял модели: выберите модель провайдера для каждой задачи",
            tone = Status.Tone.ERROR,
        )
        is SaveModelsResult.Saved -> Status(
            text = "Сохранено",
            tone = Status.Tone.SUCCESS,
        )
    }

    private fun DataState.taskRow(
        providers: List<Provider>,
        task: ModelTask,
    ): UiState.TaskRow {
        val choice = choices.getValue(task)
        val provider = providers.first { provider -> provider.code == choice.provider }
        return UiState.TaskRow(
            mark = provider.mark(),
            model = choice.model,
            models = provider.models,
            provider = provider.option(),
            task = task,
            title = task.title(),
            usage = task.usage(),
        )
    }

    private fun Provider.mark(): Status? = if (connected) null else NOT_CONNECTED

    private fun Provider.option(): UiState.ProviderOption = UiState.ProviderOption(
        code = code,
        name = name,
    )
}
