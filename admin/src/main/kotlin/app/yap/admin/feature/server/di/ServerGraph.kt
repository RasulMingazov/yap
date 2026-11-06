package app.yap.admin.feature.server.di

import app.yap.admin.feature.server.data.repository.PreferencesServerSettingsRepository
import app.yap.admin.feature.server.data.repository.RemoteServerRepository
import app.yap.admin.feature.server.domain.usecase.DefaultConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.DefaultGetServerSettingsUseCase
import app.yap.admin.feature.server.presentation.ServerViewModel
import io.ktor.client.HttpClient

internal fun serverViewModel(httpClient: HttpClient): ServerViewModel {
    val serverSettingsRepository = PreferencesServerSettingsRepository()
    return ServerViewModel(
        connectToServerUseCase = DefaultConnectToServerUseCase(
            serverRepository = RemoteServerRepository(httpClient = httpClient),
            serverSettingsRepository = serverSettingsRepository,
        ),
        getServerSettingsUseCase = DefaultGetServerSettingsUseCase(
            serverSettingsRepository = serverSettingsRepository,
        ),
    )
}
