package app.yap.admin.core.network

import app.yap.admin.core.network.settings.StubServerSettingsRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class ServerCallTest {

    @Test
    fun `GIVEN no saved settings WHEN calling the server THEN no request goes out and the call fails`() = runTest {
        val serverSettingsRepository = StubServerSettingsRepository(settings = null)
        val engine = MockEngine { respondOk() }
        val httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine)

        val result = serverSettingsRepository.callServer(
            failed = FAILED,
            read = { LOADED },
            send = { httpClient.get("/admin/levels") },
        )

        assertEquals(expected = FAILED to 0, actual = result to engine.requestHistory.size)
    }

    private companion object {

        const val FAILED = "failed"
        const val LOADED = "loaded"
    }
}
