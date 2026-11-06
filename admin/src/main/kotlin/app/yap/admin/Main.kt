package app.yap.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
            title = "Yap Admin",
        ) {
            MaterialTheme {
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
