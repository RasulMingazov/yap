package app.yap.admin.feature.server.data.repository

import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.StubServerSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class RemoteServerRepositoryTest {

    @Test
    fun `GIVEN the server accepts the key WHEN connecting THEN it is connected to the server environment`() = runTest {
        var authorization: String? = null
        val engine = MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respond(
                content = """{"environment":"staging"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val connection = RemoteServerRepository(adminHttpClient(engine)).connect(StubServerSettings.stubServerSettings())

        assertEquals(
            expected = ServerConnection.Connected(environment = "staging") to "Bearer ${StubServerSettings.KEY}",
            actual = connection to authorization,
        )
    }

    @Test
    fun `GIVEN the server rejects the key WHEN connecting THEN it is unauthorized`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }

        val connection = RemoteServerRepository(adminHttpClient(engine)).connect(StubServerSettings.stubServerSettings())

        assertEquals(expected = ServerConnection.Unauthorized, actual = connection)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN connecting THEN it is unreachable`() = runTest {
        val engine = MockEngine { throw IOException("Connection refused") }

        val connection = RemoteServerRepository(adminHttpClient(engine)).connect(StubServerSettings.stubServerSettings())

        assertEquals(expected = ServerConnection.Unreachable, actual = connection)
    }
}
