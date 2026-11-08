package app.yap.admin.navigation

import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.navigation.NavigationViewModel.DataState
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.NavigationViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NavigationUiStateMapperTest {

    @Test
    fun `GIVEN a character is open WHEN mapping THEN the sidebar shows Characters as the current section`() {
        val screen = Screen.Character(CharacterId("2c7a5f1e-9a8b-4c3d-8e2f-1a2b3c4d5e6f"))

        val uiState = NavigationUiStateMapper.map(DataState(screen = screen))

        assertEquals(
            expected = UiState.Content(
                screen = screen,
                section = Screen.Characters,
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN a new topic is open WHEN mapping THEN the sidebar shows Topics as the current section`() {
        val screen = Screen.Topic(topicId = null)

        val uiState = NavigationUiStateMapper.map(DataState(screen = screen))

        assertEquals(
            expected = UiState.Content(
                screen = screen,
                section = Screen.Topics,
            ),
            actual = uiState,
        )
    }
}
