package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.domain.usecase.StubGetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.StubSaveLevelsUseCase
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Event
import app.yap.admin.feature.levels.presentation.LevelsViewModel.News
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.domain.usecase.StubGetModelsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield

internal class LevelsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the levels WHEN Levels opens THEN B1 is the chosen level`() = runTest(dispatcher) {
        val env = Environment()

        advanceUntilIdle()

        assertEquals(expected = "Блок промпта · B1", actual = env.content().block.title)
    }

    @Test
    fun `GIVEN the levels WHEN B2 is clicked THEN its row is marked and its block shows`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.LevelClicked(LevelCode.B2))
        advanceUntilIdle()

        val content = env.content()
        assertEquals(
            expected = listOf(LevelCode.B2),
            actual = content.rows
                .filter { row -> row.isChosen }
                .map { row -> row.code },
        )
        assertEquals(expected = "Блок промпта · B2", actual = content.block.title)
    }

    @Test
    fun `GIVEN an edited level WHEN saving THEN the forms of every level are sent`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val form = StubLevel.stubInput(maxWords = "16a")
        val forms = StubLevel
            .stubLevels()
            .toInputs()

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.C1, form = form))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(forms + (LevelCode.C1 to form))
    }

    @Test
    fun `GIVEN a filled form WHEN the save succeeds THEN the table shows the server ranges`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val saved = StubLevel.stubLevels(range = StubLevel.stubRange(to = null))
        env.saveLevelsUseCase.invokeCall.returns(SaveLevelsResult.Saved(saved))

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = "1050+" to Status(text = "Сохранено", tone = Status.Tone.SUCCESS),
            actual = env.content().rows.first().range to env.content().saveStatus,
        )
    }

    @Test
    fun `GIVEN a rating that does not grow WHEN the server rejects the save THEN it asks to fix the levels`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveLevelsResult.Invalid)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = "Сервер не принял уровни: заполните все поля, числа больше нуля, рейтинг растёт от A1 к C2",
                actual = env.content().saveStatus?.text,
            )
        }

    @Test
    fun `GIVEN a save in progress WHEN a field changes THEN the edit is ignored`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveLevelsResult.Invalid)
        advanceUntilIdle()
        val form = StubLevel.stubInput(block = "New block")

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = form))
        advanceUntilIdle()

        assertEquals(expected = StubLevel.BLOCK, actual = env.content().block.form.block)
    }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN a snackbar says the save failed`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveLevelsResult.Failed)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."),
            actual = env.viewModel.news.first(),
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN Save is clicked again THEN the levels are saved once`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.called(times = 1)
    }

    @Test
    fun `GIVEN a save answer without C2 WHEN the save succeeds THEN it shows Saved and loads the levels again`() =
        runTest(dispatcher) {
            val levels = StubLevel
                .stubLevels()
                .filterNot { level -> level.code == LevelCode.C2 }
            val env = Environment(saveResult = SaveLevelsResult.Saved(levels))
            advanceUntilIdle()

            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.getLevelsUseCase.invokeCall.called(times = 2)
            assertEquals(expected = "Сохранено", actual = env.content().saveStatus?.text)
        }

    @Test
    fun `GIVEN a save answer with only B1 WHEN Save is clicked during the reload THEN the levels are saved once`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveLevelsResult.Saved(listOf(StubLevel.stubLevel())))
            advanceUntilIdle()

            env.viewModel.onEvent(Event.SaveClicked)
            yield()
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.called(times = 1)
        }

    @Test
    fun `GIVEN Levels was open WHEN it opens again THEN it loads the levels again`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        env.getLevelsUseCase.invokeCall.called(times = 2)
    }

    @Test
    fun `GIVEN the levels did not load WHEN Retry is clicked THEN the table shows them`() = runTest(dispatcher) {
        val env = Environment()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(StubLevel.stubLevels()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = LevelCode.entries, actual = env.content().rows.map { row -> row.code })
    }

    @Test
    fun `GIVEN a server answer without C2 WHEN Levels opens THEN it shows the load error`() = runTest(dispatcher) {
        val env = Environment()
        val levels = StubLevel
            .stubLevels()
            .filterNot { level -> level.code == LevelCode.C2 }
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(levels))

        advanceUntilIdle()

        assertEquals(
            expected = UiState.Error(message = "Не удалось загрузить уровни"),
            actual = env.viewModel.uiState.value,
        )
    }

    @Test
    fun `GIVEN a server answer without C2 WHEN Retry is clicked THEN the table shows every level`() =
        runTest(dispatcher) {
            val env = Environment()
            val levels = StubLevel
                .stubLevels()
                .filterNot { level -> level.code == LevelCode.C2 }
            env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(levels))
            advanceUntilIdle()
            env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(StubLevel.stubLevels()))

            env.viewModel.onEvent(Event.RetryClicked)
            advanceUntilIdle()

            assertEquals(expected = LevelCode.entries, actual = env.content().rows.map { row -> row.code })
        }

    @Test
    fun `GIVEN the models WHEN Levels opens THEN Model offers Default and every model`() = runTest(dispatcher) {
        val env = Environment()

        advanceUntilIdle()

        assertEquals(
            expected = listOf(null, StubModels.MODEL, PRO),
            actual = env.content().models.map { option -> option.model },
        )
    }

    @Test
    fun `GIVEN the models do not load WHEN Levels opens THEN the table shows with the models error`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)

            advanceUntilIdle()

            val content = env.content()
            assertEquals(
                expected = listOf(LevelCode.entries, false, "Не удалось загрузить модели. Сохранение не меняет модели уровней"),
                actual = listOf(content.rows.map { row -> row.code }, content.isModelChoiceEnabled, content.modelError),
            )
        }

    @Test
    fun `GIVEN the models did not load WHEN models Retry is clicked THEN each level shows its model`() =
        runTest(dispatcher) {
            val levels = StubLevel.stubLevels(settings = StubLevel.stubSettings(model = PRO))
            val env = Environment()
            env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(levels))
            env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)
            advanceUntilIdle()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels()))

            env.viewModel.onEvent(Event.ModelsRetryClicked)
            advanceUntilIdle()

            assertEquals(
                expected = List(LevelCode.entries.size) { PRO },
                actual = env.content().rows.map { row -> row.model.model },
            )
            env.getLevelsUseCase.invokeCall.called(times = 1)
        }

    @Test
    fun `GIVEN the models WHEN models Retry is clicked THEN the models are not loaded again`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.ModelsRetryClicked)
        advanceUntilIdle()

        env.getModelsUseCase.invokeCall.called(times = 1)
    }

    @Test
    fun `GIVEN a failed models load WHEN a second load answers with the models THEN the error hides`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)
            launch { env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels())) }

            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            assertEquals(expected = null, actual = env.content().modelError)
        }

    @Test
    fun `GIVEN the levels do not load WHEN Levels opens THEN it shows the load error`() = runTest(dispatcher) {
        val env = Environment()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Failed)

        advanceUntilIdle()

        assertEquals(
            expected = UiState.Error(message = "Не удалось загрузить уровни"),
            actual = env.viewModel.uiState.value,
        )
    }

    @Test
    fun `GIVEN the levels WHEN B1 gets a model THEN only the row of B1 shows it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = StubLevel.stubInput(model = PRO)))
        advanceUntilIdle()

        assertEquals(
            expected = listOf(null, null, PRO, null, null, null, null),
            actual = env.content().rows.map { row -> row.model.model },
        )
    }

    @Test
    fun `GIVEN B1 got a model WHEN saving THEN B1 is sent with the model`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val form = StubLevel.stubInput(model = PRO)
        val forms = StubLevel
            .stubLevels()
            .toInputs()

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = form))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(forms + (LevelCode.B1 to form))
    }

    @Test
    fun `GIVEN B1 has a model WHEN Default is saved for B1 THEN B1 is sent without a model`() = runTest(dispatcher) {
        val levels = StubLevel.stubLevels(settings = StubLevel.stubSettings(model = PRO))
        val env = Environment()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(levels))
        advanceUntilIdle()
        val form = StubLevel.stubInput(model = null)

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = form))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(levels.toInputs() + (LevelCode.B1 to form))
    }

    @Test
    fun `GIVEN the models did not load WHEN saving THEN each level is sent with its stored model`() =
        runTest(dispatcher) {
            val levels = StubLevel.stubLevels(settings = StubLevel.stubSettings(model = PRO))
            val env = Environment()
            env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(levels))
            env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.calledWith(levels.toInputs())
        }

    @Test
    fun `GIVEN a level model is no longer available WHEN Refresh is clicked THEN it loads both and hides Refresh`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveLevelsResult.ModelUnavailable)
            advanceUntilIdle()
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.RefreshClicked)
            advanceUntilIdle()

            env.getLevelsUseCase.invokeCall.called(times = 2)
            env.getModelsUseCase.invokeCall.called(times = 2)
            assertEquals(expected = false, actual = env.content().isRefreshShown)
        }

    @Test
    fun `GIVEN a Refresh in progress WHEN Save is clicked twice THEN the levels are saved once`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()

            env.viewModel.onEvent(Event.RefreshClicked)
            launch { env.viewModel.onEvent(Event.SaveClicked) }
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.called(times = 1)
        }

    @Test
    fun `GIVEN a level model is no longer available WHEN Refresh is clicked THEN the reload status shows beside a disabled Save`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveLevelsResult.ModelUnavailable)
            advanceUntilIdle()
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.RefreshClicked)
            yield()

            assertEquals(
                expected = Status(text = "Обновление…", tone = Status.Tone.MUTED) to false,
                actual = env.content().saveStatus to env.content().isSaveEnabled,
            )
        }

    @Test
    fun `GIVEN the models did not load WHEN models Retry is clicked THEN the loading sign shows until they answer`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)
            advanceUntilIdle()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels()))

            env.viewModel.onEvent(Event.ModelsRetryClicked)
            yield()
            val during = env.content().modelProgress
            advanceUntilIdle()

            assertEquals(
                expected = listOf("Загрузка моделей…", null),
                actual = listOf(during, env.content().modelProgress),
            )
        }

    private class Environment(
        saveResult: SaveLevelsResult = SaveLevelsResult.Saved(StubLevel.stubLevels()),
    ) {

        val getLevelsUseCase = StubGetLevelsUseCase()
        val getModelsUseCase = StubGetModelsUseCase()
        val saveLevelsUseCase = StubSaveLevelsUseCase(result = saveResult)
        val viewModel = LevelsViewModel(
            getLevelsUseCase = getLevelsUseCase,
            getModelsUseCase = getModelsUseCase,
            saveLevelsUseCase = saveLevelsUseCase,
        )

        init {
            viewModel.onEvent(Event.Opened)
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }

    private companion object {

        const val PRO = "deepseek-v4-pro"
    }
}
