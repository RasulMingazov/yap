package app.yap.admin.feature.models.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.domain.TaskModel
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class ModelsViewModelTest {

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
    fun `GIVEN saved models WHEN Models opens THEN Review shows the saved model`() = runTest(dispatcher) {
        val env = Environment()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = tasksWithReview(PRO))))

        advanceUntilIdle()

        assertEquals(expected = PRO, actual = env.reviewRow().model)
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
    fun `GIVEN two providers WHEN Review gets the other one THEN it takes its first model`() = runTest(dispatcher) {
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

        assertEquals(
            expected = "other-mini" to "OTHER",
            actual = env.reviewRow().model to env.reviewRow().provider.code,
        )
    }

    @Test
    fun `GIVEN the models WHEN Review gets another model THEN its row shows it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.changeReview(PRO)
        advanceUntilIdle()

        assertEquals(expected = PRO, actual = env.reviewRow().model)
    }

    @Test
    fun `GIVEN a changed model WHEN saving THEN every task is sent with it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()

        env.changeReview(PRO)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveModelsUseCase.invokeCall.calledWith(tasksWithReview(PRO))
    }

    @Test
    fun `GIVEN the server accepts WHEN saving THEN its choices and Saved show`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.Saved(StubModels.stubModels(tasks = tasksWithReview(PRO))))
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = PRO to "Сохранено",
            actual = env.reviewRow().model to env.content().saveStatus?.text,
        )
    }

    @Test
    fun `GIVEN the Saved status WHEN a model changes THEN the status goes away`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.changeReview(PRO)
        advanceUntilIdle()

        assertEquals(expected = null, actual = env.content().saveStatus)
    }

    @Test
    fun `GIVEN a save in progress WHEN a model changes THEN the change is ignored`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.Invalid)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.changeReview(PRO)
        advanceUntilIdle()

        assertEquals(expected = StubModels.MODEL, actual = env.reviewRow().model)
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

            env.changeReview(PRO)
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = "Сервер не принял модели: выберите модель провайдера для каждой задачи" to PRO,
                actual = env.content().saveStatus?.text to env.reviewRow().model,
            )
        }

    @Test
    fun `GIVEN a changed catalog WHEN the server answers 409 THEN Refresh shows`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.CatalogChanged)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(expected = true, actual = env.content().isRefreshShown)
    }

    @Test
    fun `GIVEN a changed catalog WHEN Refresh is clicked THEN the server choices show`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.CatalogChanged)
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()
        env.getModelsUseCase.invokeCall.returns(LoadResult.Loaded(StubModels.stubModels(tasks = tasksWithReview(PRO))))

        env.viewModel.onEvent(Event.RefreshClicked)
        advanceUntilIdle()

        assertEquals(
            expected = PRO to false,
            actual = env.reviewRow().model to env.content().isRefreshShown,
        )
    }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN a snackbar says the save failed`() = runTest(dispatcher) {
        val env = Environment(saveResult = SaveModelsResult.Failed)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."),
            actual = env.viewModel.news.first(),
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

    private fun tasksWithReview(model: String): List<TaskModel> = StubModels
        .stubTasks()
        .map { taskModel ->
            if (taskModel.task == ModelTask.TOPIC_REVIEW) {
                taskModel.copy(choice = taskModel.choice.copy(model = model))
            } else {
                taskModel
            }
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

        fun changeReview(model: String) {
            viewModel.onEvent(
                Event.ModelChanged(
                    model = model,
                    task = ModelTask.TOPIC_REVIEW,
                ),
            )
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content

        fun reviewRow(): UiState.TaskRow = content().tasks.first { row -> row.task == ModelTask.TOPIC_REVIEW }
    }

    private companion object {

        const val PRO = "deepseek-v4-pro"
    }
}
