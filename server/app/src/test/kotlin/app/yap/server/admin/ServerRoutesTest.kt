package app.yap.server.admin

import app.yap.server.StubAppConfig
import app.yap.server.StubAppModules
import app.yap.server.config.AppEnvironment
import app.yap.server.serverModule
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ServerRoutesTest {

    @Test
    fun `GIVEN no admin key WHEN asking for the server THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/server")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN asking for the server THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/server") { bearerAuth("wrong-key") }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN the admin key WHEN asking for the server THEN it answers with its environment`() = testApplication {
        val env = Environment(environment = AppEnvironment.STAGING)
        application { env.install(this) }

        val response = client.get("/admin/server") { bearerAuth(StubAppConfig.ADMIN_API_KEY) }

        assertEquals(
            expected = HttpStatusCode.OK to """{"environment":"staging"}""",
            actual = response.status to response.bodyAsText(),
        )
    }

    private class Environment(
        environment: AppEnvironment = AppEnvironment.TEST,
    ) {

        val config = StubAppConfig.stubAppConfig(environment = environment)

        fun install(application: Application) {
            application.serverModule(
                config = config,
                modules = StubAppModules.stubAppModules(),
            )
        }
    }
}
