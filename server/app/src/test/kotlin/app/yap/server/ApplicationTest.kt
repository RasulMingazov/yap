package app.yap.server

import app.yap.server.core.database.withTestDatabase
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ApplicationTest {

    @Test
    fun `GIVEN the server WHEN asking for its health THEN it answers ok`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/health")

        assertEquals(expected = HttpStatusCode.OK to "ok", actual = response.status to response.bodyAsText())
    }

    @Test
    fun `GIVEN no admin key WHEN listing the characters THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/characters")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN no admin key WHEN listing the levels THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/levels")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN saving the levels THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.put("/admin/levels") {
            bearerAuth("wrong-key")
            contentType(ContentType.Application.Json)
            setBody("[]")
        }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN saving the models THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.put("/admin/models") {
            bearerAuth("wrong-key")
            contentType(ContentType.Application.Json)
            setBody("[]")
        }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN listing the prompts THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/prompts") {
            bearerAuth("wrong-key")
        }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a wrong admin key WHEN saving a prompt version THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/prompts/TOPIC_REPLY/versions") {
            bearerAuth("wrong-key")
            contentType(ContentType.Application.Json)
            setBody(PROMPT_TEXT_JSON)
        }

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN no admin key WHEN listing the topics THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/topics")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a body that is not a profile WHEN creating a character THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/characters") {
            bearerAuth(StubAppConfig.ADMIN_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(NAME_ONLY_JSON)
        }

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN an identifier that is not a UUID WHEN reading a character THEN the server answers 404`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/characters/sam") {
            bearerAuth(StubAppConfig.ADMIN_API_KEY)
        }

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN an empty database WHEN the server opens it THEN the migrations of every feature apply`() {
        withTestDatabase { }
    }

    private class Environment {

        fun install(application: Application) {
            application.serverModule(
                config = StubAppConfig.stubAppConfig(),
                modules = StubAppModules.stubAppModules(),
            )
        }
    }

    private companion object {

        const val NAME_ONLY_JSON = """{"name": "Sam"}"""
        const val PROMPT_TEXT_JSON = """{"text": "Play Sam."}"""
    }
}
