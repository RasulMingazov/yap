package app.yap.admin.navigation

import app.yap.admin.navigation.NavigationViewModel.DataState
import app.yap.admin.navigation.NavigationViewModel.UiState

internal object NavigationUiStateMapper {

    fun map(state: DataState): UiState {
        val screen = state.screen ?: return UiState.Loading
        return UiState.Content(screen = screen)
    }
}
