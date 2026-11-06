package app.yap.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.server.di.serverViewModel
import app.yap.admin.feature.server.presentation.ui.ServerScreen

fun main() {
    val httpClient = adminHttpClient()
    application {
        val viewModel = remember { serverViewModel(httpClient = httpClient) }
        Window(
            onCloseRequest = {
                httpClient.close()
                exitApplication()
            },
            title = "Yap Admin",
        ) {
            MaterialTheme {
                ServerScreen(viewModel = viewModel)
            }
        }
    }
}
