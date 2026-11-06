package app.yap.admin.feature.characters.presentation

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.domain.usecase.StubGetCharactersUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class CharactersViewModelTest {

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
    fun `GIVEN the server has characters WHEN the screen opens THEN it shows them`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())))

        advanceUntilIdle()

        assertIs<CharactersViewModel.UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the characters did not load WHEN Retry is clicked THEN they load again`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Failed)
        advanceUntilIdle()
        env.getCharactersUseCase.invokeCall.returns(LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(CharactersViewModel.Event.RetryClicked)
        advanceUntilIdle()

        assertIs<CharactersViewModel.UiState.Empty>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the characters are loading WHEN Retry is clicked THEN they load once`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(CharactersViewModel.Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getCharactersUseCase.invokeCall.callCount)
    }

    private class Environment(
        characters: LoadResult<List<Character>>,
    ) {

        val getCharactersUseCase = StubGetCharactersUseCase(characters = characters)
        val viewModel = CharactersViewModel(getCharactersUseCase = getCharactersUseCase)
    }
}
