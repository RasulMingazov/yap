package app.yap.admin.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.characters.presentation.ui.CharacterScreen
import app.yap.admin.feature.characters.presentation.ui.CharactersScreen
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import app.yap.admin.feature.levels.presentation.ui.LevelsScreen
import app.yap.admin.feature.server.presentation.ServerViewModel
import app.yap.admin.feature.server.presentation.ui.ServerScreen
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicViewModel
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import app.yap.admin.feature.topics.presentation.ui.TopicScreen
import app.yap.admin.feature.topics.presentation.ui.TopicsScreen
import app.yap.admin.navigation.NavigationViewModel
import app.yap.admin.navigation.NavigationViewModel.Event
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.NavigationViewModel.UiState

@Composable
internal fun AdminShell(
    characterViewModel: (CharacterId?) -> CharacterViewModel,
    charactersViewModel: CharactersViewModel,
    levelsViewModel: LevelsViewModel,
    serverViewModel: ServerViewModel,
    topicViewModel: (TopicId?) -> TopicViewModel,
    topicsViewModel: TopicsViewModel,
    viewModel: NavigationViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val serverUiState by serverViewModel.uiState.collectAsStateWithLifecycle()
    val onScreenClick: (Screen) -> Unit = { screen -> viewModel.onEvent(Event.ScreenClicked(screen)) }
    when (val state = uiState) {
        is UiState.Content -> AdminFrame(
            current = state.section,
            onScreenClick = onScreenClick,
            serverLabel = serverUiState.serverLabel,
        ) {
            ScreenContent(
                characterViewModel = characterViewModel,
                charactersViewModel = charactersViewModel,
                levelsViewModel = levelsViewModel,
                onScreenClick = onScreenClick,
                screen = state.screen,
                serverViewModel = serverViewModel,
                topicViewModel = topicViewModel,
                topicsViewModel = topicsViewModel,
            )
        }
        is UiState.Loading -> Unit
    }
}

@Composable
private fun ScreenContent(
    characterViewModel: (CharacterId?) -> CharacterViewModel,
    charactersViewModel: CharactersViewModel,
    levelsViewModel: LevelsViewModel,
    onScreenClick: (Screen) -> Unit,
    screen: Screen,
    serverViewModel: ServerViewModel,
    topicViewModel: (TopicId?) -> TopicViewModel,
    topicsViewModel: TopicsViewModel,
) {
    when (screen) {
        is Screen.Character -> CharacterScreen(
            onBackClick = { onScreenClick(Screen.Characters) },
            viewModel = remember(screen) { characterViewModel(screen.characterId) },
        )
        is Screen.Characters -> CharactersScreen(
            onCharacterClick = { characterId -> onScreenClick(Screen.Character(characterId)) },
            onNewCharacterClick = { onScreenClick(Screen.Character(characterId = null)) },
            viewModel = charactersViewModel,
        )
        is Screen.Levels -> LevelsScreen(viewModel = levelsViewModel)
        is Screen.Server -> ServerScreen(viewModel = serverViewModel)
        is Screen.Topic -> TopicScreen(
            onBackClick = { onScreenClick(Screen.Topics) },
            viewModel = remember(screen) { topicViewModel(screen.topicId) },
        )
        is Screen.Topics -> TopicsScreen(
            onNewTopicClick = { onScreenClick(Screen.Topic(topicId = null)) },
            onTopicClick = { topicId -> onScreenClick(Screen.Topic(topicId)) },
            viewModel = topicsViewModel,
        )
    }
}
