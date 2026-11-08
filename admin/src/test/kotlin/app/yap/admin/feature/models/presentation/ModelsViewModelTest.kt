package app.yap.admin.feature.models.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.domain.TaskModel
import app.yap.admin.feature.models.domain.usecase.StubGetModelsUseCase
import app.yap.admin.feature.models.presentation.ModelsViewModel.Event
import app.yap.admin.feature.models.presentation.ModelsViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
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

        env.viewModel.onEvent(
            Event.ModelChanged(
                model = PRO,
                task = ModelTask.TOPIC_REVIEW,
            ),
        )
        advanceUntilIdle()

        assertEquals(expected = PRO, actual = env.reviewRow().model)
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

    private class Environment {

        val getModelsUseCase = StubGetModelsUseCase()
        val viewModel = ModelsViewModel(getModelsUseCase = getModelsUseCase)

        init {
            viewModel.onEvent(Event.Opened)
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content

        fun reviewRow(): UiState.TaskRow = content().tasks.first { row -> row.task == ModelTask.TOPIC_REVIEW }
    }

    private companion object {

        const val PRO = "deepseek-v4-pro"
    }
}
