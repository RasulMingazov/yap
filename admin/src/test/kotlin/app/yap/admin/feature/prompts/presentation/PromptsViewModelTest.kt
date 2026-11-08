package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.StubPrompt
import app.yap.admin.feature.prompts.domain.usecase.StubGetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.StubGetPromptsUseCase
import app.yap.admin.feature.prompts.domain.usecase.StubRestorePromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.StubSavePromptUseCase
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Event
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.News
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
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

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertEquals(expected = "Реплика персонажа", actual = env.editor().title)
    }

    @Test
    fun `GIVEN a typed text WHEN the owner returns to the prompt THEN the editor shows it`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getPromptUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPrompt(PromptKey.TOPIC_CHECK)))

        env.viewModel.onEvent(Event.TextChanged("Typed text"))
        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_CHECK))
        advanceUntilIdle()
        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_REPLY))
        advanceUntilIdle()

        assertEquals(expected = "Typed text", actual = env.editor().text)
    }

    @Test
    fun `GIVEN a prompt is loading WHEN another prompt is clicked THEN the click is ignored`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getPromptUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPrompt(PromptKey.TOPIC_CHECK)))

        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_CHECK))
        env.viewModel.onEvent(Event.PromptClicked(PromptKey.TOPIC_HINTS))
        advanceUntilIdle()

        assertEquals(expected = "Проверка шага", actual = env.editor().title)
    }

    @Test
    fun `GIVEN a typed text WHEN Save is clicked THEN the text is sent as it is`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.TextChanged(" Typed text "))
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.savePromptUseCase.invokeCall.calledWith(PromptKey.TOPIC_REPLY, " Typed text ")
    }

    @Test
    fun `GIVEN a typed text WHEN the save succeeds THEN the editor shows the saved text and Saved`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.savePromptUseCase.invokeCall.returns(SavePromptResult.Saved(StubPrompt.stubPrompt(text = "Saved")))

            env.viewModel.onEvent(Event.TextChanged("Typed text"))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = "Saved" to "Сохранено",
                actual = env.editor().text to env.content().saveStatus.text,
            )
        }

    @Test
    fun `GIVEN an empty editor WHEN the server rejects the save THEN the status asks for the text`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.savePromptUseCase.invokeCall.returns(SavePromptResult.EmptyText)

            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(expected = "Введите текст промпта", actual = env.content().saveStatus.text)
        }

    @Test
    fun `GIVEN a save in progress WHEN the text changes THEN the edit is ignored`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.savePromptUseCase.invokeCall.returns(SavePromptResult.EmptyText)

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.TextChanged("Typed text"))
        advanceUntilIdle()

        assertEquals(expected = StubPrompt.TEXT, actual = env.editor().text)
    }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN the editor stays and a snackbar shows`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.savePromptUseCase.invokeCall.returns(SavePromptResult.Failed)

            env.viewModel.onEvent(Event.TextChanged("Typed text"))
            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(
                expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз.") to "Typed text",
                actual = env.viewModel.news.first() to env.editor().text,
            )
        }

    @Test
    fun `GIVEN a typed text WHEN version 2 is restored THEN the editor shows its text`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        val restored = StubPrompt.stubPrompt(text = "Version 2")
        env.restorePromptUseCase.invokeCall.returns(RestorePromptResult.Restored(restored))

        env.viewModel.onEvent(Event.TextChanged("Typed text"))
        env.viewModel.onEvent(Event.RestoreClicked(2))
        advanceUntilIdle()

        assertEquals(expected = "Version 2", actual = env.editor().text)
    }

    @Test
    fun `GIVEN a prompt WHEN version 2 is restored THEN its key and number are sent`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.RestoreClicked(2))
        advanceUntilIdle()

        env.restorePromptUseCase.invokeCall.calledWith(PromptKey.TOPIC_REPLY, 2)
    }

    @Test
    fun `GIVEN a save in progress WHEN Save and Restore are clicked THEN the prompt is sent once`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.SaveClicked)
            env.viewModel.onEvent(Event.SaveClicked)
            env.viewModel.onEvent(Event.RestoreClicked(2))
            advanceUntilIdle()

            env.savePromptUseCase.invokeCall.called(times = 1)
            env.restorePromptUseCase.invokeCall.notCalled()
        }

    @Test
    fun `GIVEN the server does not answer WHEN restoring THEN a snackbar says the restore failed`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.restorePromptUseCase.invokeCall.returns(RestorePromptResult.Failed)

            env.viewModel.onEvent(Event.RestoreClicked(1))
            advanceUntilIdle()

            assertEquals(
                expected = News.ShowSnackbar("Не удалось вернуть версию. Попробуйте ещё раз."),
                actual = env.viewModel.news.first(),
            )
        }

    @Test
    fun `GIVEN version 2 WHEN the restore succeeds THEN the status says v2 is with the users again`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            env.viewModel.onEvent(Event.RestoreClicked(2))
            advanceUntilIdle()

            assertEquals(
                expected = Status(text = "v2 снова у пользователей", tone = Status.Tone.SUCCESS),
                actual = env.content().saveStatus,
            )
        }

    @Test
    fun `GIVEN the chosen prompt does not load WHEN Prompts opens THEN only the editor shows the error`() =
        runTest(dispatcher) {
            val env = Environment()
            env.getPromptUseCase.invokeCall.returns(LoadResult.Failed)

            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            assertEquals(
                expected = UiState.Editor.Error(message = "Не удалось загрузить промпт"),
                actual = env.content().editor,
            )
        }

    @Test
    fun `GIVEN the chosen prompt did not load WHEN Save is clicked THEN nothing is sent`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        env.getPromptUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.savePromptUseCase.invokeCall.notCalled()
    }

    @Test
    fun `GIVEN the chosen prompt did not load WHEN Retry is clicked THEN the editor shows it`() =
        runTest(dispatcher) {
            val env = Environment()
            env.viewModel.onEvent(Event.Opened)
            env.getPromptUseCase.invokeCall.returns(LoadResult.Failed)
            advanceUntilIdle()
            env.getPromptUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPrompt()))

            env.viewModel.onEvent(Event.RetryClicked)
            advanceUntilIdle()

            assertEquals(expected = StubPrompt.TEXT, actual = env.editor().text)
        }

    @Test
    fun `GIVEN the list did not load WHEN Retry is clicked THEN the editor shows the prompt`() = runTest(dispatcher) {
        val env = Environment()
        env.viewModel.onEvent(Event.Opened)
        env.getPromptsUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getPromptsUseCase.invokeCall.returns(LoadResult.Loaded(StubPrompt.stubPromptSummaries()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = StubPrompt.TEXT, actual = env.editor().text)
    }

    private class Environment {

        val getPromptUseCase = StubGetPromptUseCase()
        val getPromptsUseCase = StubGetPromptsUseCase()
        val restorePromptUseCase = StubRestorePromptUseCase()
        val savePromptUseCase = StubSavePromptUseCase()
        val viewModel = PromptsViewModel(
            getPromptUseCase = getPromptUseCase,
            getPromptsUseCase = getPromptsUseCase,
            restorePromptUseCase = restorePromptUseCase,
            savePromptUseCase = savePromptUseCase,
        )

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content

        fun editor(): UiState.Editor.Loaded = content().editor as UiState.Editor.Loaded
    }
}
