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
    fun `GIVEN No rating without a rating from WHEN saving THEN every level is sent`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val rated = LevelCode.entries.dropLast(1).map { code -> StubLevel.stubEntry(code = code) }
        val noRating = StubLevel.stubEntry(LevelCode.NO_RATING, StubLevel.stubSettings(ratingFrom = null))

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(rated + noRating)
    }

    @Test
    fun `GIVEN A1 without a rating from WHEN saving THEN A1 is sent without it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        val withoutRatingFrom = StubLevel.stubSettings(ratingFrom = null)
        val rated = listOf(LevelCode.A2, LevelCode.B1, LevelCode.B2, LevelCode.C1, LevelCode.C2)
            .map { code -> StubLevel.stubEntry(code = code) }

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.A1, form = StubLevel.stubInput(ratingFrom = "")))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.calledWith(
            listOf(StubLevel.stubEntry(LevelCode.A1, withoutRatingFrom)) +
                rated +
                StubLevel.stubEntry(LevelCode.NO_RATING, withoutRatingFrom),
        )
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
                expected = "Заполните все уровни, рейтинг должен расти от A1 к C2",
                actual = env.content().saveStatus?.text,
            )
        }

    @Test
    fun `GIVEN a word count that is not a number WHEN saving THEN it names the level and sends nothing`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()
            val form = StubLevel.stubInput()

            env.viewModel.onEvent(Event.FormChanged(code = LevelCode.NO_RATING, form = form.copy(maxWords = "16a")))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.notCalled()
            assertEquals(expected = "Проверьте число в «Слов в фразе»: Без рейтинга", actual = env.content().saveStatus?.text)
        }

    @Test
    fun `GIVEN a rating from that is not a number WHEN saving THEN it names the level and sends nothing`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()
            val form = StubLevel.stubInput()

            env.viewModel.onEvent(Event.FormChanged(code = LevelCode.A2, form = form.copy(ratingFrom = "10a")))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.notCalled()
            assertEquals(expected = "Проверьте число в «Рейтинг»: A2", actual = env.content().saveStatus?.text)
        }

    @Test
    fun `GIVEN an empty word count WHEN saving THEN it names the level and sends nothing`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = StubLevel.stubInput(maxWords = "")))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveLevelsUseCase.invokeCall.notCalled()
        assertEquals(expected = "Проверьте число в «Слов в фразе»: B1", actual = env.content().saveStatus?.text)
    }

    @Test
    fun `GIVEN a level without idioms WHEN saving THEN it asks to choose them and sends nothing`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()

            env.viewModel.onEvent(Event.FormChanged(code = LevelCode.C1, form = StubLevel.stubInput(idioms = null)))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveLevelsUseCase.invokeCall.notCalled()
            assertEquals(expected = "Выберите идиомы: C1", actual = env.content().saveStatus?.text)
        }

    @Test
    fun `GIVEN a save in progress WHEN a field changes THEN the edit is ignored`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveLevelsResult.Invalid)
        advanceUntilIdle()
        val form = StubLevel.stubInput()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.FormChanged(code = LevelCode.B1, form = form.copy(block = "New block")))
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

    private class Environment(
        saveResult: SaveLevelsResult = SaveLevelsResult.Saved(StubLevel.stubLevels()),
    ) {

        val getLevelsUseCase = StubGetLevelsUseCase()
        val saveLevelsUseCase = StubSaveLevelsUseCase(result = saveResult)
        val viewModel = LevelsViewModel(
            getLevelsUseCase = getLevelsUseCase,
            saveLevelsUseCase = saveLevelsUseCase,
        )

        init {
            viewModel.onEvent(Event.Opened)
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }
}
