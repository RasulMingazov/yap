package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.domain.usecase.StubGetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.StubSaveLevelsUseCase
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Event
import app.yap.admin.feature.levels.presentation.LevelsViewModel.LevelForm
import app.yap.admin.feature.levels.presentation.LevelsViewModel.News
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

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
    fun `GIVEN the levels WHEN B2 is clicked THEN its prompt block shows`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.LevelClicked(LevelCode.B2))
        advanceUntilIdle()

        assertEquals(expected = "Блок промпта · B2", actual = env.content().block.title)
    }

    @Test
    fun `GIVEN levels before a save WHEN saving THEN a blank rating from is sent as none`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val form = LevelForm.of(StubLevel.stubSettings())

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.NO_RATING, form = form.copy(ratingFrom = "")))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(
            LevelCode.entries.dropLast(1).map { code -> StubLevel.stubEntry(code = code) } + StubLevel.stubEntry(
                code = LevelCode.NO_RATING,
                settings = StubLevel.stubSettings(ratingFrom = null),
            ),
        )
    }

    @Test
    fun `GIVEN a filled form WHEN the save succeeds THEN the table shows the server ranges`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val saved = listOf(StubLevel.stubLevel(range = StubLevel.stubRange(to = null)))
        env.saveLevelsUseCase.invokeCall.returns(SaveLevelsResult.Saved(saved))

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("1050+") to Status(text = "Сохранено", tone = Status.Tone.SUCCESS),
            actual = env.content().rows.map { row -> row.range } to env.content().saveStatus,
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
                expected = Status(text = "Заполните все уровни, рейтинг должен расти от A1 к C2", tone = Status.Tone.ERROR),
                actual = env.content().saveStatus,
            )
        }

    @Test
    fun `GIVEN a word count that is not a number WHEN saving THEN it names the level and sends nothing`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()
            val form = LevelForm.of(StubLevel.stubSettings())

            env.viewModel.onEvent(Event.FormChanged(code = LevelCode.NO_RATING, form = form.copy(maxWords = "16a")))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.notCalled()
            assertEquals(
                expected = Status(text = "Проверьте числа: Без рейтинга", tone = Status.Tone.ERROR),
                actual = env.content().saveStatus,
            )
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
    fun `GIVEN the levels did not load WHEN Retry is clicked THEN the table shows them`() = runTest(dispatcher) {
        val env = Environment()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getLevelsUseCase.invokeCall.returns(LoadResult.Loaded(StubLevel.stubLevels()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = LevelCode.entries, actual = env.content().rows.map { row -> row.code })
    }

    private class Environment(
        saveResult: SaveLevelsResult = SaveLevelsResult.Saved(StubLevel.stubLevels()),
    ) {

        val getLevelsUseCase = StubGetLevelsUseCase()
        val saveLevelsUseCase = StubSaveLevelsUseCase(result = saveResult)
        val viewModel = LevelsViewModel(
            getLevelsUseCase = getLevelsUseCase,
            saveLevelsUseCase = saveLevelsUseCase,
        )

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }
}
