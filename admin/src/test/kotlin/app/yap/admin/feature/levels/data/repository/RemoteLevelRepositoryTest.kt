package app.yap.admin.feature.levels.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.levels.data.mapper.StubLevelDto
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
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

internal class RemoteLevelRepositoryTest {

    @Test
    fun `GIVEN the server lists the levels WHEN loading them THEN they are loaded`() = runTest {
        val env = Environment(answer = answer(content = "[$LEVEL_JSON]"))

        val result = env.repository.list()

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                LoadResult.Loaded(listOf(StubLevel.stubLevel())),
                HttpMethod.Get,
                "${StubServerSettings.ADDRESS}/admin/levels",
                "Bearer ${StubServerSettings.KEY}",
            ),
            actual = listOf(
                result,
                request.method,
                request.url.toString(),
                request.headers[HttpHeaders.Authorization],
            ),
        )
    }

    @Test
    fun `GIVEN no saved server settings WHEN loading the levels THEN loading fails without a call`() = runTest {
        val env = Environment(answer = answer(content = "[]"), settings = null)

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed to 0, actual = result to env.engine.requestHistory.size)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN loading the levels THEN loading fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN the server fails WHEN loading the levels THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.InternalServerError))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN a level code that the app does not know WHEN loading the levels THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "[${LEVEL_JSON.replace("\"B1\"", "\"D1\"")}]"))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN the entries WHEN saving THEN the server gets a PUT with the key and the levels`() = runTest {
        val env = Environment(answer = answer(content = "[$LEVEL_JSON]"))

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveLevelsResult.Saved(listOf(StubLevel.stubLevel())),
                HttpMethod.Put,
                "${StubServerSettings.ADDRESS}/admin/levels",
                "Bearer ${StubServerSettings.KEY}",
                Json.parseToJsonElement("""[{"code": "B1", "settings": $SETTINGS_JSON}]"""),
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
    fun `GIVEN the server rejects the table WHEN saving THEN the save is invalid`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        assertEquals(expected = SaveLevelsResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN no saved server settings WHEN saving THEN the save fails without a call`() = runTest {
        val env = Environment(answer = answer(content = "[]"), settings = null)

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        assertEquals(expected = SaveLevelsResult.Failed to 0, actual = result to env.engine.requestHistory.size)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN saving THEN the save fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        assertEquals(expected = SaveLevelsResult.Failed, actual = result)
    }

    private class Environment(
        answer: MockRequestHandler,
        settings: ServerSettings? = StubServerSettings.stubServerSettings(),
    ) {

        val engine = MockEngine(answer)
        val repository = RemoteLevelRepository(
            httpClient = adminHttpClient(engine),
            serverSettingsRepository = StubServerSettingsRepository(settings = settings),
        )
    }

    private companion object {

        const val SETTINGS_JSON = """
            {
              "block": "${StubLevel.BLOCK}",
              "idioms": "${StubLevelDto.IDIOMS}",
              "maxWords": ${StubLevel.MAX_WORDS},
              "ratingFrom": ${StubLevel.RANGE_FROM},
              "vocabulary": "${StubLevel.VOCABULARY}"
            }
        """
        const val LEVEL_JSON = """
            {
              "code": "${StubLevelDto.CODE}",
              "range": {"from": ${StubLevel.RANGE_FROM}, "to": ${StubLevel.RANGE_TO}},
              "settings": $SETTINGS_JSON
            }
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
