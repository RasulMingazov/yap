package app.yap.admin.feature.server.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.feature.server.presentation.ServerViewModel
import app.yap.admin.feature.server.presentation.ServerViewModel.Event
import app.yap.admin.feature.server.presentation.ServerViewModel.UiState

@Composable
internal fun ServerScreen(viewModel: ServerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ServerContent(
        onEvent = viewModel::onEvent,
        uiState = uiState,
    )
}

@Composable
internal fun ServerContent(
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        ScreenToolbar(
            subtitle = "Сервер, с которым работает админка",
            title = "Сервер",
        )
        ScreenBody {
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { address -> onEvent(Event.AddressChanged(address)) },
                label = { Text("Адрес сервера") },
                singleLine = true,
                modifier = Modifier.width(AdminTheme.Sizes.fieldWidth),
            )
            OutlinedTextField(
                value = uiState.key,
                onValueChange = { key -> onEvent(Event.KeyChanged(key)) },
                label = { Text("Ключ админа") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.width(AdminTheme.Sizes.fieldWidth),
            )
            Button(
                onClick = { onEvent(Event.ConnectClicked) },
                enabled = uiState.isConnectEnabled,
            ) {
                Text(uiState.connectLabel)
            }
            uiState.status?.let { status ->
                Text(text = status, color = AdminTheme.Colors.muted, style = AdminTheme.Type.body)
            }
        }
    }
}
