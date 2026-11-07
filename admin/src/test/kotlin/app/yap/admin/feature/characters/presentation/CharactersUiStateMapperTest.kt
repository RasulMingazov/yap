package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.presentation.CharactersViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CharactersUiStateMapperTest {

    @Test
    fun `GIVEN loaded characters WHEN mapping THEN each character is a row with its name and personality`() {
        val state = DataState(characters = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())))

        val uiState = CharactersUiStateMapper.map(state)

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
        val state = DataState(characters = LoadResult.Loaded(emptyList()))

        val uiState = CharactersUiStateMapper.map(state)

        assertEquals(expected = UiState.Empty(message = "Персонажей пока нет"), actual = uiState)
    }

    @Test
    fun `GIVEN the characters did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(characters = LoadResult.Failed)

        val uiState = CharactersUiStateMapper.map(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить персонажей"), actual = uiState)
    }
}
