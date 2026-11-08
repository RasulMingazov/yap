package app.yap.admin.feature.server.presentation

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.usecase.StubConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.StubGetServerSettingsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class ServerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN saved settings WHEN the screen opens THEN it connects and shows the server`() = runTest(dispatcher) {
        val savedSettings = StubServerSettings.stubServerSettings()
        val env = Environment(
            connection = ServerConnection.Connected(environment = "staging"),
            savedSettings = savedSettings,
        )

        advanceUntilIdle()

        assertEquals(expected = "Сервер: staging", actual = env.viewModel.uiState.value.serverLabel)
        env.connectToServerUseCase.invokeCall.calledWith(savedSettings)
    }

    @Test
    fun `GIVEN an entered address and key WHEN connect is clicked THEN it connects with the entered values`() =
        runTest(dispatcher) {
            val env = Environment(connection = ServerConnection.Unauthorized, savedSettings = null)
            advanceUntilIdle()

            env.viewModel.onEvent(ServerViewModel.Event.AddressChanged(" http://staging.yap "))
            env.viewModel.onEvent(ServerViewModel.Event.KeyChanged(" staging-key "))
            env.viewModel.onEvent(ServerViewModel.Event.ConnectClicked)
            advanceUntilIdle()

            env.connectToServerUseCase.invokeCall.calledWith(
                ServerSettings(address = " http://staging.yap ", key = " staging-key "),
            )
        }

    @Test
    fun `GIVEN a connection in progress WHEN connect is clicked again THEN it connects once`() = runTest(dispatcher) {
        val env = Environment(connection = ServerConnection.Unreachable, savedSettings = null)
        advanceUntilIdle()
        env.viewModel.onEvent(ServerViewModel.Event.KeyChanged("staging-key"))

        env.viewModel.onEvent(ServerViewModel.Event.ConnectClicked)
        env.viewModel.onEvent(ServerViewModel.Event.ConnectClicked)
        advanceUntilIdle()

        env.connectToServerUseCase.invokeCall.called(times = 1)
    }

    @Test
    fun `GIVEN a key typed before the saved settings load WHEN they load THEN the typed key stays`() =
        runTest(dispatcher) {
            val env = Environment(
                connection = ServerConnection.Connected(environment = "staging"),
                savedSettings = StubServerSettings.stubServerSettings(),
            )

            env.viewModel.onEvent(ServerViewModel.Event.KeyChanged("typed-key"))
            advanceUntilIdle()

            assertEquals(expected = "typed-key", actual = env.viewModel.uiState.value.key)
            env.connectToServerUseCase.invokeCall.notCalled()
        }

    @Test
    fun `GIVEN an address typed before the saved settings load WHEN they load THEN the typed address stays`() =
        runTest(dispatcher) {
            val env = Environment(
                connection = ServerConnection.Connected(environment = "staging"),
                savedSettings = StubServerSettings.stubServerSettings(),
            )

            env.viewModel.onEvent(ServerViewModel.Event.AddressChanged("http://typed.yap"))
            advanceUntilIdle()

            assertEquals(expected = "http://typed.yap", actual = env.viewModel.uiState.value.address)
            env.connectToServerUseCase.invokeCall.notCalled()
        }

    @Test
    fun `GIVEN a key typed and cleared before the saved settings load WHEN they load THEN the key stays empty`() =
        runTest(dispatcher) {
            val env = Environment(
                connection = ServerConnection.Connected(environment = "staging"),
                savedSettings = StubServerSettings.stubServerSettings(),
            )

            env.viewModel.onEvent(ServerViewModel.Event.KeyChanged("typed-key"))
            env.viewModel.onEvent(ServerViewModel.Event.KeyChanged(""))
            advanceUntilIdle()

            assertEquals(expected = "", actual = env.viewModel.uiState.value.key)
            env.connectToServerUseCase.invokeCall.notCalled()
        }

    private class Environment(
        connection: ServerConnection,
        savedSettings: ServerSettings?,
    ) {

        val connectToServerUseCase = StubConnectToServerUseCase(connection = connection)
        val getServerSettingsUseCase = StubGetServerSettingsUseCase(settings = savedSettings)
        val viewModel = ServerViewModel(
            connectToServerUseCase = connectToServerUseCase,
            getServerSettingsUseCase = getServerSettingsUseCase,
        )
    }
}
