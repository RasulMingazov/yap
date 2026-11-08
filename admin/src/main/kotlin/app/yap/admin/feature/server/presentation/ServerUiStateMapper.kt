package app.yap.admin.feature.server.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.presentation.ServerViewModel.DataState
import app.yap.admin.feature.server.presentation.ServerViewModel.UiState

internal object ServerUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState = UiState(
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

    private fun ServerConnection.status(): Status = when (this) {
        is ServerConnection.Connected -> Status(text = "Подключено", tone = Status.Tone.SUCCESS)
        is ServerConnection.Unauthorized -> Status(text = "Сервер не принял ключ", tone = Status.Tone.ERROR)
        is ServerConnection.Unreachable -> Status(text = "Сервер не отвечает", tone = Status.Tone.ERROR)
    }
}
