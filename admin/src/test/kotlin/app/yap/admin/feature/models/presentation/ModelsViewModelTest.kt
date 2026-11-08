package app.yap.admin.feature.models.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.StubModels
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

        env.viewModel.onEvent(
            Event.ModelChanged(
                model = PRO,
                task = ModelTask.TOPIC_REVIEW,
            ),
        )
        advanceUntilIdle()

        assertEquals(expected = PRO_REVIEW_MODELS, actual = env.models())
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

    private class Environment {

        val getModelsUseCase = StubGetModelsUseCase()
        val viewModel = ModelsViewModel(getModelsUseCase = getModelsUseCase)

        init {
            viewModel.onEvent(Event.Opened)
        }

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content

        fun models(): List<String> = content().tasks.map { row -> row.model }
    }

    private companion object {

        const val PRO = "deepseek-v4-pro"

        val PRO_REVIEW_MODELS = listOf(StubModels.MODEL, StubModels.MODEL, StubModels.MODEL, PRO, StubModels.MODEL)
    }
}
