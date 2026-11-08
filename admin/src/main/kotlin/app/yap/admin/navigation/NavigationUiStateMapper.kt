package app.yap.admin.navigation

import app.yap.admin.navigation.NavigationViewModel.DataState
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.NavigationViewModel.UiState

internal object NavigationUiStateMapper {

    fun map(state: DataState): UiState {
        val screen = state.screen ?: return UiState.Loading
        return UiState.Content(
            screen = screen,
            section = screen.section(),
        )
    }

    private fun Screen.section(): Screen = when (this) {
        is Screen.Character -> Screen.Characters
        is Screen.Topic -> Screen.Topics
        is Screen.Characters,
        is Screen.Levels,
        is Screen.Server,
        is Screen.Topics,
        -> this
    }
}
