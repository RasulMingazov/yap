package app.yap.admin.feature.characters.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.core.network.adminKtorfit
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.characters.data.api.createCharacterApi
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
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
        val env = Environment(answer = answer(content = "[$SUMMARY_JSON]"))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Loaded(listOf(StubCharacter.stubCharacter())), actual = result)
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
                ContentType.Application.Json,
            ),
            actual = listOf(
                result,
                request.method,
                request.url.toString(),
                request.headers[HttpHeaders.Authorization],
                Json.parseToJsonElement(request.body.toByteArray().decodeToString()),
                request.body.contentType,
            ),
        )
    }

    @Test
    fun `GIVEN a saved character WHEN updating it THEN the server gets a PUT at its address`() = runTest {
        val env = Environment(answer = answer(content = CHARACTER_JSON))

        env.repository.update(StubCharacter.stubCharacter())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                HttpMethod.Put,
                "${StubServerSettings.ADDRESS}/admin/characters/${StubCharacter.CHARACTER_ID}",
                ContentType.Application.Json,
            ),
            actual = listOf(request.method, request.url.toString(), request.body.contentType),
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

    private class Environment(answer: MockRequestHandler) {

        val engine = MockEngine(answer)
        val serverSettingsRepository = StubServerSettingsRepository(settings = StubServerSettings.stubServerSettings())
        private val ktorfit = adminKtorfit(
            httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine),
        )
        val repository = RemoteCharacterRepository(
            characterApi = ktorfit.createCharacterApi(),
            serverSettingsRepository = serverSettingsRepository,
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
        const val SUMMARY_JSON = """
            {"characterId": "${StubCharacter.CHARACTER_ID}", "profile": $PROFILE_JSON, "topicCount": 2}
        """

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
