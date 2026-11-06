package app.yap.server.admin

import app.yap.server.StubAppConfig
import app.yap.server.core.config.AppEnvironment
import app.yap.server.serverModule
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ServerRoutesTest {

    @Test
    fun `GIVEN no admin key WHEN asking for the server THEN the call is unauthorized`() = testApplication {
        application { serverModule(StubAppConfig.stubAppConfig()) }

        val response = client.get("/admin/server")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN asking for the server THEN the call is unauthorized`() = testApplication {
        application { serverModule(StubAppConfig.stubAppConfig()) }

        val response = client.get("/admin/server") { bearerAuth("wrong-key") }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN the admin key WHEN asking for the server THEN it answers with its environment`() = testApplication {
        application { serverModule(StubAppConfig.stubAppConfig(environment = AppEnvironment.STAGING)) }

        val response = client.get("/admin/server") { bearerAuth(StubAppConfig.ADMIN_API_KEY) }

        assertEquals(
            expected = HttpStatusCode.OK to """{"environment":"staging"}""",
            actual = response.status to response.bodyAsText(),
        )
    }
}
