package app.yap.admin.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.server.domain.usecase.GetServerSettingsUseCase
import app.yap.admin.feature.topics.domain.TopicId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NavigationViewModel(
    private val getServerSettingsUseCase: GetServerSettingsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, NavigationUiStateMapper::map)

    init {
        openStartScreen()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.ScreenClicked -> dataState.update { state -> state.copy(screen = event.screen) }
    }

    private fun openStartScreen() {
        viewModelScope.launch {
            val start = if (getServerSettingsUseCase() == null) Screen.Server else Screen.Topics
            dataState.update { state -> state.copy(screen = state.screen ?: start) }
        }
    }

    data class DataState(
        val screen: Screen? = null,
    )

    sealed interface UiState {

        data class Content(
            val screen: Screen,
            val section: Screen,
        ) : UiState

        data object Loading : UiState
    }

    sealed interface Screen {

        data class Character(val characterId: CharacterId?) : Screen

        data object Characters : Screen

        data object Server : Screen

        data class Topic(val topicId: TopicId?) : Screen

        data object Topics : Screen
    }

    sealed interface Event {

        data class ScreenClicked(val screen: Screen) : Event
    }
}
