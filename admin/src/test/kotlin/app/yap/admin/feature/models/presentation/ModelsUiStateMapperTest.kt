package app.yap.admin.feature.models.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.models.domain.ModelChoice
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models
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
    fun `GIVEN Review with another provider WHEN mapping THEN its Model offers the models of that provider`() {
        val other = StubModels.stubProvider(
            code = "OTHER",
            models = listOf("other-mini"),
            name = "Other",
        )
        val state = loaded(StubModels.stubModels(providers = listOf(StubModels.stubProvider(), other)))
        val review = ModelTask.TOPIC_REVIEW to ModelChoice(
            model = "other-mini",
            provider = "OTHER",
        )

        val uiState = ModelsUiStateMapper(state.copy(choices = state.choices + review)) as UiState.Content

        assertEquals(
            expected = UiState.ProviderOption(
                code = "OTHER",
                name = "Other",
            ) to listOf("other-mini"),
            actual = uiState.tasks[ModelTask.TOPIC_REVIEW.ordinal].let { row -> row.provider to row.models },
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

    private fun loaded(models: Models): DataState = DataState(
        choices = models.tasks.associate { taskModel -> taskModel.task to taskModel.choice },
        models = models,
    )
}
