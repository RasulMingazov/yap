package app.yap.admin.feature.server.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.presentation.ServerViewModel.DataState
import app.yap.admin.feature.server.presentation.ServerViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ServerUiStateMapperTest {

    @Test
    fun `GIVEN a connected server WHEN mapping THEN the sidebar names its environment`() {
        val state = DataState(
            address = "http://localhost:8080",
            connection = ServerConnection.Connected(environment = "staging"),
            key = "key",
        )

        val uiState = ServerUiStateMapper.map(state)

        assertEquals(
            expected = UiState(
                address = "http://localhost:8080",
                connectLabel = "Подключиться",
                isConnectEnabled = true,
                key = "key",
                serverLabel = "Сервер: staging",
                status = Status(text = "Подключено", tone = Status.Tone.SUCCESS),
            ),
            actual = uiState,
        )
    }

    @Test
    fun `GIVEN a rejected key WHEN mapping THEN the status says so and the server is not connected`() {
        val uiState = ServerUiStateMapper.map(DataState(connection = ServerConnection.Unauthorized, key = "key"))

        assertEquals(
            expected = "Сервер: не подключён" to Status(text = "Сервер не принял ключ", tone = Status.Tone.ERROR),
            actual = uiState.serverLabel to uiState.status,
        )
    }

    @Test
    fun `GIVEN a connection in progress WHEN mapping THEN connect is disabled`() {
        val uiState = ServerUiStateMapper.map(DataState(isConnecting = true, key = "key"))

        assertEquals(expected = "Подключаемся…" to false, actual = uiState.connectLabel to uiState.isConnectEnabled)
    }

    @Test
    fun `GIVEN no key WHEN mapping THEN connect is disabled`() {
        val uiState = ServerUiStateMapper.map(DataState())

        assertEquals(expected = false, actual = uiState.isConnectEnabled)
    }
}
