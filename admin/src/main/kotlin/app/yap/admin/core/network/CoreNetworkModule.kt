package app.yap.admin.core.network

import app.yap.admin.core.network.settings.PreferencesServerSettingsRepository
import app.yap.admin.core.network.settings.ServerSettingsRepository
import java.util.prefs.Preferences
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

internal fun coreNetworkModule(): Module = module {
    single { Preferences.userRoot().node("app/yap/admin") }
    singleOf(::PreferencesServerSettingsRepository) { bind<ServerSettingsRepository>() }
    single(createdAtStart = true) { adminHttpClient(serverSettingsRepository = get()) } onClose { httpClient ->
        httpClient?.close()
    }
}
