package app.yap.admin.core.network

import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class ServerSettingsPluginTest {

    @Test
    fun `GIVEN saved settings WHEN sending a request THEN it goes to the saved address with the saved key`() = runTest {
        val env = Environment()

        env.httpClient.get("/admin/levels")

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = "$SERVER_ADDRESS/admin/levels" to "Bearer ${StubServerSettings.KEY}",
            actual = request.url.toString() to request.headers[HttpHeaders.Authorization],
        )
    }

    @Test
    fun `GIVEN a request with its own key WHEN sending it THEN its address and key stay`() = runTest {
        val env = Environment()

        env.httpClient.get("$CHECKED_ADDRESS/admin/server") { bearerAuth(CHECKED_KEY) }

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = "$CHECKED_ADDRESS/admin/server" to "Bearer $CHECKED_KEY",
            actual = request.url.toString() to request.headers[HttpHeaders.Authorization],
        )
    }

    private class Environment {

        val engine = MockEngine { respondOk() }
        val httpClient = adminHttpClient(
            serverSettingsRepository = StubServerSettingsRepository(
                settings = StubServerSettings.stubServerSettings(address = "$SERVER_ADDRESS/"),
            ),
            engine = engine,
        )
    }

    private companion object {

        const val CHECKED_ADDRESS = "http://localhost:9090"
        const val CHECKED_KEY = "new-admin-key"
        const val SERVER_ADDRESS = "https://yap.example:8443/api"
    }
}
