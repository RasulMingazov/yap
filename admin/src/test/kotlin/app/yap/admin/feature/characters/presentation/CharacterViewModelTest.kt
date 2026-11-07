package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.domain.usecase.StubGetCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.StubSaveCharacterUseCase
import app.yap.admin.feature.characters.presentation.CharacterViewModel.Event
import app.yap.admin.feature.characters.presentation.CharacterViewModel.News
import app.yap.admin.feature.characters.presentation.CharacterViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class CharacterViewModelTest {

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
    fun `GIVEN a saved character WHEN the form opens THEN the fields hold its profile`() = runTest(dispatcher) {
        val env = Environment(characterId = StubCharacter.stubCharacter().characterId)

        advanceUntilIdle()

        assertEquals(expected = StubCharacter.stubProfile(), actual = env.content().form)
    }

    @Test
    fun `GIVEN the server does not answer WHEN the form opens THEN it shows the load error`() = runTest(dispatcher) {
        val env = Environment(characterId = StubCharacter.stubCharacter().characterId)
        env.getCharacterUseCase.invokeCall.returns(LoadResult.Failed)

        advanceUntilIdle()

        assertIs<UiState.Error>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the character did not load WHEN Retry is clicked THEN the form shows it`() = runTest(dispatcher) {
        val env = Environment(characterId = StubCharacter.stubCharacter().characterId)
        env.getCharacterUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getCharacterUseCase.invokeCall.returns(LoadResult.Loaded(StubCharacter.stubCharacter()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = StubCharacter.stubProfile(), actual = env.content().form)
    }

    @Test
    fun `GIVEN a new character WHEN the save succeeds THEN its name becomes the title`() = runTest(dispatcher) {
        val env = Environment(characterId = null)
        env.viewModel.onEvent(Event.FormChanged(StubCharacter.stubProfile()))

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(expected = "Sam" to Status(text = "Сохранено", tone = Status.Tone.SUCCESS), actual = env.content().title to env.content().saveStatus)
    }

    @Test
    fun `GIVEN an empty field WHEN the server rejects the save THEN the form asks to fill in every field`() =
        runTest(dispatcher) {
            val env = Environment(characterId = null, saveResult = SaveCharacterResult.Invalid)

            env.viewModel.onEvent(Event.SaveClicked)
            advanceUntilIdle()

            assertEquals(expected = Status(text = "Заполните все поля", tone = Status.Tone.ERROR), actual = env.content().saveStatus)
        }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN a snackbar says the save failed`() = runTest(dispatcher) {
        val env = Environment(characterId = null, saveResult = SaveCharacterResult.Failed)

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."),
            actual = env.viewModel.news.first(),
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN Save is clicked again THEN the character is saved once`() = runTest(dispatcher) {
        val env = Environment(characterId = null)

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveCharacterUseCase.invokeCall.called(times = 1)
    }

    private class Environment(
        characterId: CharacterId?,
        saveResult: SaveCharacterResult = SaveCharacterResult.Saved(StubCharacter.stubCharacter()),
    ) {

        val getCharacterUseCase = StubGetCharacterUseCase()
        val saveCharacterUseCase = StubSaveCharacterUseCase(result = saveResult)
        val viewModel = CharacterViewModel(
            characterId = characterId,
            getCharacterUseCase = getCharacterUseCase,
            saveCharacterUseCase = saveCharacterUseCase,
        )

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }
}
