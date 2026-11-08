package app.yap.admin.feature.characters.presentation

import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.presentation.CharactersViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CharactersUiStateMapperTest {

    @Test
    fun `GIVEN loaded characters WHEN mapping THEN each character is a row with its name and personality`() {
        val state = DataState(characters = listOf(StubCharacter.stubCharacter()))

        val uiState = CharactersUiStateMapper(state)

        assertEquals(
            expected = UiState.Content(
                rows = listOf(
                    UiState.Content.Row(
                        characterId = CharacterId(StubCharacter.CHARACTER_ID),
                        name = "Sam",
                        personality = "Friendly and curious.",
                    ),
                ),
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN no characters WHEN mapping THEN the screen says that there are none yet`() {
        val state = DataState(characters = emptyList())

        val uiState = CharactersUiStateMapper(state)

        assertEquals(expected = UiState.Empty(message = "Персонажей пока нет"), actual = uiState)
    }

    @Test
    fun `GIVEN the characters did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(isLoadFailed = true)

        val uiState = CharactersUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить персонажей"), actual = uiState)
    }

    @Test
    fun `GIVEN a shown list and a failed reload WHEN mapping THEN the list stays`() {
        val state = DataState(characters = listOf(StubCharacter.stubCharacter()), isLoadFailed = true)

        val uiState = CharactersUiStateMapper(state)

        assertIs<UiState.Content>(uiState)
    }

    @Test
    fun `GIVEN the characters are not loaded yet WHEN mapping THEN the screen is loading`() {
        val state = DataState(isLoading = true)

        val uiState = CharactersUiStateMapper(state)

        assertEquals(expected = UiState.Loading, actual = uiState)
    }
}
