package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.presentation.CharacterViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharacterViewModel.Load
import app.yap.admin.feature.characters.presentation.CharacterViewModel.SaveStatus
import app.yap.admin.feature.characters.presentation.CharacterViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CharacterUiStateMapperTest {

    @Test
    fun `GIVEN a new character WHEN mapping THEN the form is empty with the hint to fill it in`() {
        val state = DataState(characterId = null)

        val uiState = CharacterUiStateMapper(state)

        assertEquals(
            expected = UiState.Content(
                form = StubCharacter.stubProfile(history = "", manner = "", name = "", personality = ""),
                hint = "Заполните поля и нажмите «Сохранить».",
                isSaveEnabled = true,
                saveStatus = null,
                subtitle = "Ещё не сохранён",
                title = "Новый персонаж",
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN a saved character WHEN mapping THEN its name is the title and the status says it is saved`() {
        val character = StubCharacter.stubCharacter()
        val state = DataState(characterId = character.characterId)
            .saved(character.profile)
            .copy(saveStatus = SaveStatus.SAVED)

        val uiState = CharacterUiStateMapper(state)

        assertEquals(
            expected = UiState.Content(
                form = character.profile,
                hint = null,
                isSaveEnabled = true,
                saveStatus = Status(text = "Сохранено", tone = Status.Tone.SUCCESS),
                subtitle = "Постоянный герой",
                title = "Sam",
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN the character did not load WHEN mapping THEN the form shows the error`() {
        val state = DataState(characterId = StubCharacter.stubCharacter().characterId).copy(load = Load.FAILED)

        val uiState = CharacterUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить персонажа"), actual = uiState)
    }
}
