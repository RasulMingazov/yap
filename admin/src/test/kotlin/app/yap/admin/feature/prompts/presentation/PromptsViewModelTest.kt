package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.StubPrompt
import app.yap.admin.feature.prompts.domain.usecase.StubGetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.StubGetPromptsUseCase
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Event
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
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

internal class PromptsViewModelTest {

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
    fun `GIVEN the prompts WHEN Prompts opens THEN Character reply is the chosen prompt`() = runTest(dispatcher) {
        val env = Environment()

        advanceUntilIdle()

        assertEquals(expected = "Реплика персонажа", actual = env.content().title)
    }

    @Test
    fun `GIVEN a typed text WHEN the owner returns to the prompt THEN the editor shows it`() = runTest(dispatcher) {
        val env = Environment()
        advanceUntilIdle()
        env.getPromptUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPrompt(PromptKey.TOPIC_CHECK)))

        env.viewModel.onEvent(Event.TextChanged("Typed text"))
        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_CHECK))
        advanceUntilIdle()
        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_REPLY))
        advanceUntilIdle()

        assertEquals(expected = "Typed text", actual = env.content().text)
    }

    @Test
    fun `GIVEN the chosen prompt does not load WHEN Prompts opens THEN it shows the load error`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getPromptUseCase.invokeCall.returns(LoadResult.Failed)

            advanceUntilIdle()

            assertEquals(
                expected = UiState.Error(message = "Не удалось загрузить промпты"),
                actual = env.viewModel.uiState.value,
            )
        }

    @Test
    fun `GIVEN the list did not load WHEN Retry is clicked THEN the editor shows the prompt`() = runTest(dispatcher) {
        val env = Environment()
        env.getPromptsUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getPromptsUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPromptSummaries()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = StubPrompt.TEXT, actual = env.content().text)
    }

    private class Environment {

        val getPromptUseCase = StubGetPromptUseCase()
        val getPromptsUseCase = StubGetPromptsUseCase()
        val viewModel = PromptsViewModel(
            getPromptUseCase = getPromptUseCase,
            getPromptsUseCase = getPromptsUseCase,
        )

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }
}
