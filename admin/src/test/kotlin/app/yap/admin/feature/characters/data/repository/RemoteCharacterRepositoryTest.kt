package app.yap.admin.feature.characters.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.StubServerSettings
import app.yap.admin.feature.server.domain.repository.StubServerSettingsRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

internal class RemoteCharacterRepositoryTest {

    @Test
    fun `GIVEN the server lists characters WHEN loading them THEN they are loaded`() = runTest {
        val env = Environment(answer = answer(content = "[$CHARACTER_JSON]"))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())), actual = result)
    }

    @Test
    fun `GIVEN no saved server settings WHEN loading the characters THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "[]"), settings = null)

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed to 0, actual = result to env.engine.requestHistory.size)
    }

    @Test
    fun `GIVEN a new profile WHEN creating a character THEN the server gets the profile with the key`() = runTest {
        val env = Environment(answer = answer(content = CHARACTER_JSON, status = HttpStatusCode.Created))

        val result = env.repository.create(StubCharacter.stubProfile())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveCharacterResult.Saved(StubCharacter.stubCharacter()),
                HttpMethod.Post,
                "${StubServerSettings.ADDRESS}/admin/characters",
                "Bearer ${StubServerSettings.KEY}",
                Json.parseToJsonElement(PROFILE_JSON),
            ),
            actual = listOf(
                result,
                request.method,
                request.url.toString(),
                request.headers[HttpHeaders.Authorization],
                Json.parseToJsonElement(request.body.toByteArray().decodeToString()),
            ),
        )
    }

    @Test
    fun `GIVEN a saved character WHEN updating it THEN the server gets a PUT at its address`() = runTest {
        val env = Environment(answer = answer(content = CHARACTER_JSON))

        env.repository.update(StubCharacter.stubCharacter())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = HttpMethod.Put to "${StubServerSettings.ADDRESS}/admin/characters/${StubCharacter.CHARACTER_ID}",
            actual = request.method to request.url.toString(),
        )
    }

    @Test
    fun `GIVEN the server rejects the profile WHEN updating a character THEN the profile is invalid`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.update(StubCharacter.stubCharacter())

        assertEquals(expected = SaveCharacterResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN loading a character THEN loading fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.find(StubCharacter.stubCharacter().characterId)

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    private class Environment(
        answer: MockRequestHandler,
        settings: ServerSettings? = StubServerSettings.stubServerSettings(),
    ) {

        val engine = MockEngine(answer)
        val repository = RemoteCharacterRepository(
            httpClient = adminHttpClient(engine),
            serverSettingsRepository = StubServerSettingsRepository(settings = settings),
        )
    }

    private companion object {

        const val PROFILE_JSON = """
            {
              "history": "${StubCharacter.HISTORY}",
              "manner": "${StubCharacter.MANNER}",
              "name": "${StubCharacter.NAME}",
              "personality": "${StubCharacter.PERSONALITY}"
            }
        """
        const val CHARACTER_JSON = """{"characterId": "${StubCharacter.CHARACTER_ID}", "profile": $PROFILE_JSON}"""

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
