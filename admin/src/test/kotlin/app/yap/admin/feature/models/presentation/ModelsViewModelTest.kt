package app.yap.admin.feature.models.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.domain.usecase.StubGetModelsUseCase
import app.yap.admin.feature.models.domain.usecase.StubSaveModelsUseCase
import app.yap.admin.feature.models.presentation.ModelsViewModel.Event
import app.yap.admin.feature.models.presentation.ModelsViewModel.News
import app.yap.admin.feature.models.presentation.ModelsViewModel.UiState
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

internal class ModelsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val proReviewTasks = listOf(
        StubModels.stubTask(task = ModelTask.TOPIC_REPLY),
        StubModels.stubTask(task = ModelTask.TOPIC_CHECK),
        StubModels.stubTask(task = ModelTask.TOPIC_HINTS),
        StubModels.stubTask(
            model = PRO,
            task = ModelTask.TOPIC_REVIEW,
        ),
        StubModels.stubTask(task = ModelTask.CALIBRATION),
    )

    @BeforeTest
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN saved models WHEN Models opens THEN every task shows its saved model`() = runTest(dispatcher) {
        val env = Environment()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = proReviewTasks)))

        advanceUntilIdle()

        assertEquals(expected = PRO_REVIEW_MODELS, actual = env.models())
    }

    @Test
    fun `GIVEN Models was open WHEN it opens again THEN it loads the models again`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        env.getModelsUseCase.invokeCall.called(times = 2)
    }

    @Test
    fun `GIVEN two providers WHEN Review gets the other one THEN only Review takes its first model`() =
        runTest(dispatcher) {
            val env = Environment()
            val other = StubModels.stubProvider(
                code = "OTHER",
                models = listOf("other-mini", "other-max"),
                name = "Other",
            )
            val providers = listOf(StubModels.stubProvider(), other)
            env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(providers = providers)))
            advanceUntilIdle()

            env.viewModel.onEvent(
                Event.ProviderChanged(
                    provider = "OTHER",
                    task = ModelTask.TOPIC_REVIEW,
                ),
            )
            advanceUntilIdle()

            val deepSeek = StubModels.MODEL to StubModels.PROVIDER
            assertEquals(
                expected = listOf(deepSeek, deepSeek, deepSeek, "other-mini" to "OTHER", deepSeek),
                actual = env.content().tasks.map { row -> row.model to row.provider.code },
            )
        }

    @Test
    fun `GIVEN Review has deepseek-v4-pro WHEN DeepSeek is chosen again THEN it keeps deepseek-v4-pro`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = proReviewTasks)))
            advanceUntilIdle()

            env.viewModel.onEvent(
                Event.ProviderChanged(
                    provider = StubModels.PROVIDER,
                    task = ModelTask.TOPIC_REVIEW,
                ),
            )
            advanceUntilIdle()

            assertEquals(expected = PRO_REVIEW_MODELS, actual = env.models())
        }

    @Test
    fun `GIVEN the models WHEN Review gets another model THEN only its row shows it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(REVIEW_PRO)
        advanceUntilIdle()

        assertEquals(expected = PRO_REVIEW_MODELS, actual = env.models())
    }

    @Test
    fun `GIVEN a changed model WHEN saving THEN every task is sent with it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(REVIEW_PRO)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveModelsUseCase.invokeCall.calledWith(proReviewTasks)
    }

    @Test
    fun `GIVEN the server accepts WHEN saving THEN its choices and Saved show`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.Saved(StubModels.stubModels(tasks = proReviewTasks)))
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = PRO_REVIEW_MODELS to "Сохранено",
            actual = env.models() to env.content().saveStatus?.text,
        )
    }

    @Test
    fun `GIVEN the Saved status WHEN a model changes THEN the status goes away`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.viewModel.onEvent(REVIEW_PRO)
        advanceUntilIdle()

        assertEquals(expected = null, actual = env.content().saveStatus)
    }

    @Test
    fun `GIVEN a save in progress WHEN a model changes THEN the change is ignored`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.Invalid)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(REVIEW_PRO)
        advanceUntilIdle()

        assertEquals(expected = List(size = 5) { StubModels.MODEL }, actual = env.models())
    }

    @Test
    fun `GIVEN a save in progress WHEN Save is clicked again THEN the models are saved once`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveModelsUseCase.invokeCall.called(times = 1)
    }

    @Test
    fun `GIVEN a load that answers during a save WHEN Save is clicked again THEN the models are saved once`() =
        runTest(dispatcher) {
            val env = Environment()
            advanceUntilIdle()

            env.viewModel.onEvent(Event.Opened)
            launch { env.viewModel.onEvent(Event.SaveClicked) }
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            env.saveModelsUseCase.invokeCall.called(times = 1)
        }

    @Test
    fun `GIVEN a save in progress WHEN Models opens again THEN it does not load`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        env.getModelsUseCase.invokeCall.called(times = 1)
    }

    @Test
    fun `GIVEN a changed model WHEN the server answers 400 THEN it asks to fix and keeps the form`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveModelsResult.Invalid)
            advanceUntilIdle()

            env.viewModel.onEvent(REVIEW_PRO)
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = "Сервер не принял модели: выберите модель провайдера для каждой задачи" to PRO_REVIEW_MODELS,
                actual = env.content().saveStatus?.text to env.models(),
            )
        }

    @Test
    fun `GIVEN a changed catalog WHEN the server answers 409 THEN Refresh shows and the form stays`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveModelsResult.CatalogChanged)
            advanceUntilIdle()

            env.viewModel.onEvent(REVIEW_PRO)
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = true to PRO_REVIEW_MODELS,
                actual = env.content().isRefreshShown to env.models(),
            )
        }

    @Test
    fun `GIVEN a changed catalog WHEN Refresh is clicked THEN the server choices show`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.CatalogChanged)
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = proReviewTasks)))

        env.viewModel.onEvent(Event.RefreshClicked)
        advanceUntilIdle()

        assertEquals(
            expected = PRO_REVIEW_MODELS to false,
            actual = env.models() to env.content().isRefreshShown,
        )
    }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN a snackbar shows and the form stays`() =
        runTest(dispatcher) {
            val env = Environment(saveResult = SaveModelsResult.Failed)
            advanceUntilIdle()

            env.viewModel.onEvent(REVIEW_PRO)
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз.") to PRO_REVIEW_MODELS to true,
                actual = env.viewModel.news.first() to env.models() to env.content().isFormEnabled,
            )
        }

    @Test
    fun `GIVEN the models did not load WHEN Retry is clicked THEN the tasks show`() = runTest(dispatcher) {
        val env = Environment()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = ModelTask.entries, actual = env.content().tasks.map { row -> row.task })
    }

    @Test
    fun `GIVEN an answer without Hints WHEN Retry is clicked THEN the tasks show`() = runTest(dispatcher) {
        val env = Environment()
        val tasks = StubModels
            .stubTasks()
            .filterNot { taskModel -> taskModel.task == ModelTask.TOPIC_HINTS }
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = tasks)))
        advanceUntilIdle()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = ModelTask.entries, actual = env.content().tasks.map { row -> row.task })
    }

    private class Environment(
        saveResult: SaveModelsResult = SaveModelsResult.Saved(StubModels.stubModels()),
    ) {

        val getModelsUseCase = StubGetModelsUseCase()
        val saveModelsUseCase = StubSaveModelsUseCase(result = saveResult)
        val viewModel = ModelsViewModel(
            getModelsUseCase = getModelsUseCase,
            saveModelsUseCase = saveModelsUseCase,
        )

        init {
            viewModel.onEvent(Event.Opened)
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content

        fun models(): List<String> = content().tasks.map { row -> row.model }
    }

    private companion object {

        const val PRO = "deepseek-v4-pro"

        val PRO_REVIEW_MODELS = listOf(StubModels.MODEL, StubModels.MODEL, StubModels.MODEL, PRO, StubModels.MODEL)

        val REVIEW_PRO = Event.ModelChanged(
            model = PRO,
            task = ModelTask.TOPIC_REVIEW,
        )
    }
}
