package app.yap.admin

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.yap.admin.core.design.AdminMaterialTheme
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.characters.di.characterViewModel
import app.yap.admin.feature.characters.di.charactersViewModel
import app.yap.admin.feature.server.di.serverViewModel
import app.yap.admin.navigation.di.navigationViewModel
import app.yap.admin.navigation.ui.AdminShell

fun main() {
    val httpClient = adminHttpClient()
    application {
        val navigationViewModel = remember { navigationViewModel() }
        val serverViewModel = remember { serverViewModel(httpClient = httpClient) }
        Window(
            onCloseRequest = {
                httpClient.close()
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
                    characterViewModel = { characterId ->
                        characterViewModel(
                            characterId = characterId,
                            httpClient = httpClient,
                        )
                    },
                    charactersViewModel = { charactersViewModel(httpClient = httpClient) },
                    serverViewModel = serverViewModel,
                    viewModel = navigationViewModel,
                )
            }
        }
    }
}
