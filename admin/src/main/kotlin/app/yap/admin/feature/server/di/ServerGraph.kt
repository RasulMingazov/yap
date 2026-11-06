package app.yap.admin.feature.server.di

import app.yap.admin.feature.server.data.repository.DefaultServerRepository
import app.yap.admin.feature.server.data.repository.DefaultServerSettingsRepository
import app.yap.admin.feature.server.domain.usecase.DefaultConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.DefaultGetServerSettingsUseCase
import app.yap.admin.feature.server.presentation.ServerViewModel
import io.ktor.client.HttpClient

internal fun serverViewModel(httpClient: HttpClient): ServerViewModel {
    val serverSettingsRepository = DefaultServerSettingsRepository()
    return ServerViewModel(
        connectToServerUseCase = DefaultConnectToServerUseCase(
            serverRepository = DefaultServerRepository(httpClient = httpClient),
            serverSettingsRepository = serverSettingsRepository,
        ),
        getServerSettingsUseCase = DefaultGetServerSettingsUseCase(
            serverSettingsRepository = serverSettingsRepository,
        ),
    )
}
