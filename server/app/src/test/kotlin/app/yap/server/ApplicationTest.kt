package app.yap.server

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ApplicationTest {

    @Test
    fun `GIVEN no admin key WHEN listing the characters THEN the call is unauthorized`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/characters")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }

    @Test
    fun `GIVEN a profile with an empty field WHEN creating a character THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/characters") {
            bearerAuth(StubAppConfig.ADMIN_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(EMPTY_HISTORY_JSON)
        }

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN an unknown character WHEN reading it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.characterRepository.findCall.returns(null)
        application { env.install(this) }

        val response = client.get("/admin/characters/${StubCharacter.CHARACTER_ID.value}") {
            bearerAuth(StubAppConfig.ADMIN_API_KEY)
        }

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN an unknown character WHEN updating it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.characterRepository.updateCall.returns(false)
        application { env.install(this) }

        val response = client.put("/admin/characters/${StubCharacter.CHARACTER_ID.value}") {
            bearerAuth(StubAppConfig.ADMIN_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(PROFILE_JSON)
        }

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
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

        const val EMPTY_HISTORY_JSON = """{"history": "", "manner": "Casual.", "name": "Sam", "personality": "Friendly."}"""
        const val PROFILE_JSON = """{"history": "Moved here.", "manner": "Casual.", "name": "Sam", "personality": "Friendly."}"""
    }
}
