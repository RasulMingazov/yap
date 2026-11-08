package app.yap.admin

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.yap.admin.core.design.AdminMaterialTheme
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import app.yap.admin.feature.server.presentation.ServerViewModel
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import app.yap.admin.navigation.NavigationViewModel
import app.yap.admin.navigation.ui.AdminShell
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

fun main() {
    val koin = koinApplication { modules(appModules()) }.koin
    application {
        val navigationViewModel = remember { koin.get<NavigationViewModel>() }
        val charactersViewModel = remember { koin.get<CharactersViewModel>() }
        val serverViewModel = remember { koin.get<ServerViewModel>() }
        val topicsViewModel = remember { koin.get<TopicsViewModel>() }
        Window(
            onCloseRequest = {
                koin.close()
                exitApplication()
            },
            state = rememberWindowState(
                size = DpSize(
                    width = AdminTheme.Sizes.windowWidth,
                    height = AdminTheme.Sizes.windowHeight,
                ),
            ),
            title = "Yap Admin",
        ) {
            AdminMaterialTheme {
                AdminShell(
                    characterViewModel = { characterId -> koin.get { parametersOf(characterId) } },
                    charactersViewModel = charactersViewModel,
                    levelsViewModel = { koin.get<LevelsViewModel>() },
                    serverViewModel = serverViewModel,
                    topicViewModel = { topicId -> koin.get { parametersOf(topicId) } },
                    topicsViewModel = topicsViewModel,
                    viewModel = navigationViewModel,
                )
            }
        }
    }
}
