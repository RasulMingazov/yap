package app.yap.admin.feature.server.data.repository

import app.yap.admin.feature.server.domain.StubServerSettings
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

internal class PreferencesServerSettingsRepositoryTest {

    private val preferences = Preferences.userRoot().node("app/yap/admin-test/${UUID.randomUUID()}")

    @AfterTest
    fun removePreferences() {
        preferences.removeNode()
    }

    @Test
    fun `GIVEN saved settings WHEN reading THEN the same settings come back`() = runTest {
        val settings = StubServerSettings.stubServerSettings()
        val repository = PreferencesServerSettingsRepository(preferences = preferences)
        repository.save(settings)

        val saved = PreferencesServerSettingsRepository(preferences = preferences).get()

        assertEquals(expected = settings, actual = saved)
    }

    @Test
    fun `GIVEN nothing saved WHEN reading THEN there are no settings`() = runTest {
        val saved = PreferencesServerSettingsRepository(preferences = preferences).get()

        assertNull(saved)
    }
}
