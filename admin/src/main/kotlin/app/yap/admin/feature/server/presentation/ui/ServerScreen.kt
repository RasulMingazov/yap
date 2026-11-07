package app.yap.admin.feature.server.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.StatusText
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
            AdminTextField(
                label = "Адрес сервера",
                onValueChange = { address -> onEvent(Event.AddressChanged(address)) },
                value = uiState.address,
                modifier = Modifier.width(AdminTheme.Sizes.fieldWidth),
            )
            AdminTextField(
                label = "Ключ админа",
                onValueChange = { key -> onEvent(Event.KeyChanged(key)) },
                value = uiState.key,
                modifier = Modifier.width(AdminTheme.Sizes.fieldWidth),
                secret = true,
            )
            PrimaryButton(
                enabled = uiState.isConnectEnabled,
                onClick = { onEvent(Event.ConnectClicked) },
                text = uiState.connectLabel,
            )
            uiState.status?.let { status -> StatusText(status = status) }
        }
    }
}
