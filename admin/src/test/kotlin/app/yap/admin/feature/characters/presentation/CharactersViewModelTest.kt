package app.yap.admin.feature.characters.presentation

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.domain.usecase.StubGetCharactersUseCase
import app.yap.admin.feature.characters.presentation.CharactersViewModel.Event
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a shown list WHEN the screen opens again THEN the list stays until the new list replaces it`() =
        runTest(dispatcher) {
            val env = Environment(characters = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())))
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            val rowCounts = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                env.viewModel.uiState.collect { state -> rowCounts += (state as? UiState.Content)?.rows?.size }
            }
            env.getCharactersUseCase.invokeCall.returns(LoadResult.Loaded(CAST))

            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            assertEquals(expected = listOf<Int?>(1, 2), actual = rowCounts)
        }

    @Test
    fun `GIVEN a shown list WHEN the list does not update THEN the list stays`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())))
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getCharactersUseCase.invokeCall.returns(LoadResult.Failed)

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a load in progress WHEN the screen opens again THEN the characters load once`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getCharactersUseCase.invokeCall.callCount)
    }

    @Test
    fun `GIVEN the characters did not load WHEN Retry is clicked THEN they load again`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Failed)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getCharactersUseCase.invokeCall.returns(LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertIs<UiState.Empty>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the characters are loading WHEN Retry is clicked THEN they load once`() = runTest(dispatcher) {
        val env = Environment(characters = LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getCharactersUseCase.invokeCall.callCount)
    }

    private class Environment(
        characters: LoadResult<List<Character>>,
    ) {

        val getCharactersUseCase = StubGetCharactersUseCase(characters = characters)
        val viewModel = CharactersViewModel(getCharactersUseCase = getCharactersUseCase)
    }

    private companion object {

        val CAST = listOf(
            StubCharacter.stubCharacter(),
            StubCharacter.stubCharacter(
                characterId = "7d1e0c2a-4b3f-4e6a-9c8d-2f1a0b3c4d5e",
                profile = StubCharacter.stubProfile(name = "Mia"),
            ),
        )
    }
}
