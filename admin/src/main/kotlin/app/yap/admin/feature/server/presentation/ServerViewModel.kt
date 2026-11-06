package app.yap.admin.feature.server.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.usecase.ConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.GetServerSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ServerViewModel(
    private val connectToServerUseCase: ConnectToServerUseCase,
    private val getServerSettingsUseCase: GetServerSettingsUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, ServerUiStateMapper::map)

    init {
        loadSettings()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.AddressChanged -> dataState.update { state -> state.copy(address = event.address) }
        is Event.ConnectClicked -> onConnectClicked()
        is Event.KeyChanged -> dataState.update { state -> state.copy(key = event.key) }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = getServerSettingsUseCase() ?: return@launch
            dataState.update { state -> state.copy(address = settings.address, isConnecting = true, key = settings.key) }
            connect(settings)
        }
    }

    private fun onConnectClicked() {
        val state = dataState.value
        if (state.isConnecting) return
        dataState.update { current -> current.copy(isConnecting = true) }
        viewModelScope.launch {
            connect(ServerSettings(address = state.address.trim(), key = state.key.trim()))
        }
    }

    private suspend fun connect(settings: ServerSettings) {
        val connection = connectToServerUseCase(settings)
        dataState.update { state -> state.copy(connection = connection, isConnecting = false) }
    }

    data class DataState(
        val address: String = DEFAULT_ADDRESS,
        val connection: ServerConnection? = null,
        val isConnecting: Boolean = false,
        val key: String = "",
    )

    data class UiState(
        val address: String,
        val connectLabel: String,
        val isConnectEnabled: Boolean,
        val key: String,
        val serverLabel: String,
        val status: String?,
    )

    sealed interface Event {

        data class AddressChanged(val address: String) : Event

        data object ConnectClicked : Event

        data class KeyChanged(val key: String) : Event
    }

    private companion object {

        const val DEFAULT_ADDRESS = "http://localhost:8080"
    }
}
