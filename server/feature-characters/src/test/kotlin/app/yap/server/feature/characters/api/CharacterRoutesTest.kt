package app.yap.server.feature.characters.api

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.CreateCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharacterSummariesUseCase
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.StubCharacterTopicCounts
import app.yap.server.feature.characters.usecase.UpdateCharacterUseCase
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

internal class CharacterRoutesTest {

    @Test
    fun `GIVEN a character with two topics WHEN listing the characters THEN the row has the topic count`() =
        testApplication {
            val env = Environment()
            env.characterTopicCounts.byCharacterCall.returns(mapOf(StubCharacter.CHARACTER_ID to 2))
            application { env.install(this) }

            val response = client.get("/admin/characters")

            assertEquals(
                expected = HttpStatusCode.OK to Json.parseToJsonElement("[$SUMMARY_JSON]"),
                actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
            )
        }

    @Test
    fun `GIVEN a filled profile WHEN creating a character THEN the server answers 201 with it`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/characters") {
            contentType(ContentType.Application.Json)
            setBody(PROFILE_JSON)
        }

        assertEquals(
            expected = HttpStatusCode.Created to Json.parseToJsonElement(CHARACTER_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a profile with an empty field WHEN creating a character THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/characters") {
            contentType(ContentType.Application.Json)
            setBody(EMPTY_HISTORY_JSON)
        }

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN a character WHEN reading it THEN the server answers with its JSON`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/characters/${StubCharacter.CHARACTER_ID.value}")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(CHARACTER_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN an unknown character WHEN reading it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.characterRepository.findCall.returns(null)
        application { env.install(this) }

        val response = client.get("/admin/characters/${StubCharacter.CHARACTER_ID.value}")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a filled profile WHEN updating a character THEN the server answers with it`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.put("/admin/characters/${StubCharacter.CHARACTER_ID.value}") {
            contentType(ContentType.Application.Json)
            setBody(PROFILE_JSON)
        }

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(CHARACTER_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a profile with an empty field WHEN updating a character THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.put("/admin/characters/${StubCharacter.CHARACTER_ID.value}") {
            contentType(ContentType.Application.Json)
            setBody(EMPTY_HISTORY_JSON)
        }

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN an unknown character WHEN updating it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.characterRepository.updateCall.returns(false)
        application { env.install(this) }

        val response = client.put("/admin/characters/${StubCharacter.CHARACTER_ID.value}") {
            contentType(ContentType.Application.Json)
            setBody(PROFILE_JSON)
        }

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val characterTopicCounts = StubCharacterTopicCounts()

        fun install(application: Application) {
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                characterRoutes(
                    createCharacterUseCase = CreateCharacterUseCase(characterRepository = characterRepository),
                    getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
                    getCharacterSummariesUseCase = GetCharacterSummariesUseCase(
                        characterRepository = characterRepository,
                        characterTopicCounts = characterTopicCounts,
                    ),
                    updateCharacterUseCase = UpdateCharacterUseCase(characterRepository = characterRepository),
                )
            }
        }
    }

    private companion object {

        const val EMPTY_HISTORY_JSON = """
            {
              "history": "",
              "manner": "${StubCharacter.MANNER}",
              "name": "${StubCharacter.NAME}",
              "personality": "${StubCharacter.PERSONALITY}"
            }
        """
        const val PROFILE_JSON = """
            {
              "history": "${StubCharacter.HISTORY}",
              "manner": "${StubCharacter.MANNER}",
              "name": "${StubCharacter.NAME}",
              "personality": "${StubCharacter.PERSONALITY}"
            }
        """
        val CHARACTER_JSON = """{"characterId": "${StubCharacter.CHARACTER_ID.value}", "profile": $PROFILE_JSON}"""
        val SUMMARY_JSON = """
            {"characterId": "${StubCharacter.CHARACTER_ID.value}", "profile": $PROFILE_JSON, "topicCount": 2}
        """
    }
}
