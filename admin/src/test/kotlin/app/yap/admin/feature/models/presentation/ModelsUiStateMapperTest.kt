package app.yap.admin.feature.models.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.models.domain.ModelChoice
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.Provider
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.presentation.ModelsViewModel.DataState
import app.yap.admin.feature.models.presentation.ModelsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ModelsUiStateMapperTest {

    @Test
    fun `GIVEN no answer yet WHEN mapping THEN Models shows the loader`() {
        val uiState = ModelsUiStateMapper(DataState())

        assertEquals(expected = UiState.Loading, actual = uiState)
    }

    @Test
    fun `GIVEN the models before a save WHEN mapping THEN every task has DeepSeek deepseek-flash`() {
        val uiState = ModelsUiStateMapper(loaded(StubModels.stubModels())) as UiState.Content

        assertEquals(
            expected = List(size = 5) { "DeepSeek" to "deepseek-flash" },
            actual = uiState.tasks.map { row -> row.provider.name to row.model },
        )
    }

    @Test
    fun `GIVEN the models WHEN mapping THEN the tasks show the prompt lines and the Calibration line`() {
        val uiState = ModelsUiStateMapper(loaded(StubModels.stubModels())) as UiState.Content

        assertEquals(
            expected = listOf(
                "Реплика персонажа" to "после каждого ответа",
                "Проверка шага" to "каждый ответ",
                "Подсказки" to "по запросу пользователя",
                "Разбор и рекомендации" to "в конце темы",
                "Калибровка" to "проверка и реплики Сэма",
            ),
            actual = uiState.tasks.map { row -> row.title to row.usage },
        )
    }

    @Test
    fun `GIVEN a provider with a key WHEN mapping THEN Providers shows it with its models and Connected`() {
        val uiState = ModelsUiStateMapper(loaded(StubModels.stubModels())) as UiState.Content

        assertEquals(
            expected = UiState.ProviderRow(
                models = "deepseek-flash, deepseek-v4-pro",
                name = "DeepSeek",
                state = Status(
                    text = "Подключён",
                    tone = Status.Tone.SUCCESS,
                ),
                step = null,
            ),
            actual = uiState.providers.single(),
        )
    }

    @Test
    fun `GIVEN a provider without a key WHEN mapping THEN Not connected shows with the step to add its key`() {
        val models = StubModels.stubModels(providers = listOf(StubModels.stubProvider(connected = false)))

        val uiState = ModelsUiStateMapper(loaded(models)) as UiState.Content

        assertEquals(
            expected = UiState.ProviderRow(
                models = "deepseek-flash, deepseek-v4-pro",
                name = "DeepSeek",
                state = Status(
                    text = "Не подключён",
                    tone = Status.Tone.WARNING,
                ),
                step = "Добавьте ключ DEEPSEEK_API_KEY в настройки сервера",
            ),
            actual = uiState.providers.single(),
        )
    }

    @Test
    fun `GIVEN a task with a provider without a key WHEN mapping THEN its row shows Not connected`() {
        val models = StubModels.stubModels(providers = listOf(StubModels.stubProvider(connected = false)))

        val uiState = ModelsUiStateMapper(loaded(models)) as UiState.Content

        assertEquals(
            expected = Status(
                text = "Не подключён",
                tone = Status.Tone.WARNING,
            ),
            actual = uiState.tasks
                .first()
                .mark,
        )
    }

    @Test
    fun `GIVEN a task with a provider with a key WHEN mapping THEN its row has no mark`() {
        val uiState = ModelsUiStateMapper(loaded(StubModels.stubModels())) as UiState.Content

        assertEquals(
            expected = null,
            actual = uiState.tasks
                .first()
                .mark,
        )
    }

    @Test
    fun `GIVEN Review with deepseek-v4-pro WHEN mapping THEN its row shows deepseek-v4-pro`() {
        val state = reviewWith(
            choice = ModelChoice(
                model = "deepseek-v4-pro",
                provider = "DEEPSEEK",
            ),
            models = StubModels.stubModels(),
        )

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = "deepseek-v4-pro",
            actual = uiState.tasks
                .single { row -> row.task == ModelTask.TOPIC_REVIEW }
                .model,
        )
    }

    @Test
    fun `GIVEN Review with another provider WHEN mapping THEN its Model offers the models of that provider`() {
        val state = reviewWith(
            choice = ModelChoice(
                model = "other-mini",
                provider = "OTHER",
            ),
            models = StubModels.stubModels(providers = listOf(StubModels.stubProvider(), otherProvider())),
        )

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = UiState.TaskRow(
                mark = null,
                model = "other-mini",
                models = listOf("other-mini"),
                provider = UiState.ProviderOption(
                    code = "OTHER",
                    name = "Other",
                ),
                task = ModelTask.TOPIC_REVIEW,
                title = "Разбор и рекомендации",
                usage = "в конце темы",
            ),
            actual = uiState.tasks.single { row -> row.task == ModelTask.TOPIC_REVIEW },
        )
    }

    @Test
    fun `GIVEN two providers WHEN mapping THEN Provider offers both in the server's order`() {
        val models = StubModels.stubModels(providers = listOf(StubModels.stubProvider(), otherProvider()))

        val uiState = ModelsUiStateMapper(loaded(models)) as UiState.Content

        assertEquals(
            expected = listOf(
                UiState.ProviderOption(
                    code = "DEEPSEEK",
                    name = "DeepSeek",
                ),
                UiState.ProviderOption(
                    code = "OTHER",
                    name = "Other",
                ),
            ),
            actual = uiState.providerOptions,
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN mapping THEN the saving status shows and the form is disabled`() {
        val state = loaded(StubModels.stubModels())
            .copy(isSaving = true)

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(
                text = "Сохранение…",
                tone = Status.Tone.MUTED,
            ) to false,
            actual = uiState.saveStatus to uiState.isFormEnabled,
        )
    }

    @Test
    fun `GIVEN a saved form WHEN mapping THEN the Saved status shows beside an enabled Save`() {
        val state = loaded(StubModels.stubModels())
            .copy(saveResult = SaveModelsResult.Saved(StubModels.stubModels()))

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(
                text = "Сохранено",
                tone = Status.Tone.SUCCESS,
            ) to true,
            actual = uiState.saveStatus to uiState.isFormEnabled,
        )
    }

    @Test
    fun `GIVEN a rejected save WHEN mapping THEN the status asks to choose a model, without Refresh`() {
        val state = loaded(StubModels.stubModels())
            .copy(saveResult = SaveModelsResult.Invalid)

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(
                text = "Сервер не принял модели: выберите модель провайдера для каждой задачи",
                tone = Status.Tone.ERROR,
            ) to false,
            actual = uiState.saveStatus to uiState.isRefreshShown,
        )
    }

    @Test
    fun `GIVEN a changed catalog WHEN mapping THEN the status says the model list changed, with Refresh`() {
        val state = loaded(StubModels.stubModels())
            .copy(saveResult = SaveModelsResult.CatalogChanged)

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(
                text = "Список моделей на сервере изменился",
                tone = Status.Tone.ERROR,
            ) to true,
            actual = uiState.saveStatus to uiState.isRefreshShown,
        )
    }

    @Test
    fun `GIVEN a failed save WHEN mapping THEN no status shows and no Refresh`() {
        val state = loaded(StubModels.stubModels())
            .copy(saveResult = SaveModelsResult.Failed)

        val uiState = ModelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = null to false,
            actual = uiState.saveStatus to uiState.isRefreshShown,
        )
    }

    @Test
    fun `GIVEN the models did not load WHEN mapping THEN Models shows the load error`() {
        val uiState = ModelsUiStateMapper(DataState(isLoadFailed = true))

        assertEquals(
            expected = UiState.Error(message = "Не удалось загрузить модели"),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN an answer without Hints WHEN mapping THEN Models shows the load error`() {
        val models = StubModels.stubModels()
        val withoutHints = models.copy(
            tasks = models.tasks.filter { taskModel -> taskModel.task != ModelTask.TOPIC_HINTS },
        )

        val uiState = ModelsUiStateMapper(loaded(withoutHints))

        assertEquals(
            expected = UiState.Error(message = "Не удалось загрузить модели"),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN a Retry after an answer without tasks WHEN mapping THEN Models shows the loader`() {
        val state = loaded(StubModels.stubModels(tasks = emptyList()))
            .copy(isLoading = true)

        assertEquals(expected = UiState.Loading, actual = ModelsUiStateMapper(state))
    }

    private fun reviewWith(
        choice: ModelChoice,
        models: Models,
    ): DataState {
        val state = loaded(models)
        return state.copy(choices = state.choices + (ModelTask.TOPIC_REVIEW to choice))
    }

    private fun loaded(models: Models): DataState = DataState(
        choices = models.tasks.associate { taskModel -> taskModel.task to taskModel.choice },
        models = models,
    )

    private fun otherProvider(): Provider = StubModels.stubProvider(
        code = "OTHER",
        models = listOf("other-mini"),
        name = "Other",
    )
}
