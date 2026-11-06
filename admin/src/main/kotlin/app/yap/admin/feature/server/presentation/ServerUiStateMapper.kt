package app.yap.admin.feature.server.presentation

import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.presentation.ServerViewModel.DataState
import app.yap.admin.feature.server.presentation.ServerViewModel.UiState

internal object ServerUiStateMapper {

    fun map(state: DataState): UiState = UiState(
        address = state.address,
        connectLabel = state.connectLabel(),
        isConnectEnabled = state.isConnectEnabled(),
        key = state.key,
        serverLabel = state.connection.serverLabel(),
        status = state.connection?.status(),
    )

    private fun DataState.connectLabel(): String = if (isConnecting) "Подключаемся…" else "Подключиться"

    private fun DataState.isConnectEnabled(): Boolean = !isConnecting && address.isNotBlank() && key.isNotBlank()

    private fun ServerConnection?.serverLabel(): String = when (this) {
        is ServerConnection.Connected -> "Сервер: $environment"
        is ServerConnection.Unauthorized,
        is ServerConnection.Unreachable,
        null,
        -> "Сервер: не подключён"
    }

    private fun ServerConnection.status(): String = when (this) {
        is ServerConnection.Connected -> "Подключено"
        is ServerConnection.Unauthorized -> "Сервер не принял ключ"
        is ServerConnection.Unreachable -> "Сервер не отвечает"
    }
}
