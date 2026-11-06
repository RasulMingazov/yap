package app.yap.admin.feature.server.data.repository

import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.repository.ServerSettingsRepository
import java.util.prefs.Preferences

internal class PreferencesServerSettingsRepository(
    private val preferences: Preferences = Preferences.userRoot().node("app/yap/admin"),
) : ServerSettingsRepository {

    override suspend fun get(): ServerSettings? {
        val address = preferences.get(ADDRESS, null)
        val key = preferences.get(KEY, null)
        if (address == null || key == null) return null
        return ServerSettings(address = address, key = key)
    }

    override suspend fun save(settings: ServerSettings) {
        preferences.put(ADDRESS, settings.address)
        preferences.put(KEY, settings.key)
        preferences.flush()
    }

    private companion object {

        const val ADDRESS = "server.address"
        const val KEY = "server.key"
    }
}
