package app.yap.admin.feature.server.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.server.domain.ServerConnection
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
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, ServerUiStateMapper)

    init {
        loadSettings()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.AddressChanged -> onAddressChanged(event.address)
        is Event.ConnectClicked -> onConnectClicked()
        is Event.KeyChanged -> onKeyChanged(event.key)
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = getServerSettingsUseCase() ?: return@launch
            if (dataState.value.isEdited) return@launch
            dataState.update { state ->
                state.copy(
                    address = settings.address,
                    key = settings.key,
                )
            }
            connect(settings)
        }
    }

    private fun onAddressChanged(address: String) {
        dataState.update { state ->
            state.copy(
                address = address,
                isEdited = true,
            )
        }
    }

    private fun onConnectClicked() {
        val state = dataState.value
        val settings = ServerSettings(
            address = state.address,
            key = state.key,
        )
        connect(settings)
    }

    private fun connect(settings: ServerSettings) {
        if (dataState.value.isConnecting) return
        dataState.update { current -> current.copy(isConnecting = true) }
        viewModelScope.launch {
            val connection = connectToServerUseCase(settings)
            dataState.update { current ->
                current.copy(
                    connection = connection,
                    isConnecting = false,
                )
            }
        }
    }

    private fun onKeyChanged(key: String) {
        dataState.update { state ->
            state.copy(
                isEdited = true,
                key = key,
            )
        }
    }

    data class DataState(
        val address: String = DEFAULT_ADDRESS,
        val connection: ServerConnection? = null,
        val isConnecting: Boolean = false,
        val isEdited: Boolean = false,
        val key: String = "",
    )

    data class UiState(
        val address: String,
        val connectLabel: String,
        val isConnectEnabled: Boolean,
        val key: String,
        val serverLabel: String,
        val status: Status?,
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
