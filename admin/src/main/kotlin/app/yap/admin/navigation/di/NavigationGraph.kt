package app.yap.admin.navigation.di

import app.yap.admin.feature.server.data.repository.PreferencesServerSettingsRepository
import app.yap.admin.feature.server.domain.usecase.DefaultGetServerSettingsUseCase
import app.yap.admin.navigation.NavigationViewModel

internal fun navigationViewModel(): NavigationViewModel = NavigationViewModel(
    getServerSettingsUseCase = DefaultGetServerSettingsUseCase(
        serverSettingsRepository = PreferencesServerSettingsRepository(),
    ),
)
