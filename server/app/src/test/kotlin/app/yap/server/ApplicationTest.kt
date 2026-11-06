package app.yap.server

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
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

        val characterRepository = StubCharacterRepository()

        fun install(application: Application) {
            application.serverModule(
                characterRepository = characterRepository,
                config = StubAppConfig.stubAppConfig(),
            )
        }
    }

    private companion object {

        const val NAME_ONLY_JSON = """{"name": "Sam"}"""
    }
}
