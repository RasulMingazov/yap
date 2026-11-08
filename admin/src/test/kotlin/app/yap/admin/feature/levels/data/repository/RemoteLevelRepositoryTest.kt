package app.yap.admin.feature.levels.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.core.network.adminKtorfit
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.levels.data.api.createLevelApi
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
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

internal class RemoteLevelRepositoryTest {

    @Test
    fun `GIVEN a saved level and a level without values WHEN loading the levels THEN both are loaded`() = runTest {
        val env = Environment(answer = answer(content = "[$LEVEL_JSON, $EMPTY_LEVEL_JSON]"))

        val result = env.repository.list()

        assertEquals(
            expected = LoadResult.Loaded(
                listOf(StubLevel.stubLevel(), StubLevel.stubLevel(LevelCode.NO_RATING, range = null, settings = null)),
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN loading the levels THEN loading fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

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
    fun `GIVEN the server rejects the table WHEN saving THEN the save is invalid`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        assertEquals(expected = SaveLevelsResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN the server fails WHEN saving THEN the save fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.InternalServerError))

        val result = env.repository.save(listOf(StubLevel.stubEntry()))

        assertEquals(expected = SaveLevelsResult.Failed, actual = result)
    }

    private class Environment(answer: MockRequestHandler) {

        val engine = MockEngine(answer)
        val serverSettingsRepository = StubServerSettingsRepository(settings = StubServerSettings.stubServerSettings())
        private val ktorfit = adminKtorfit(
            httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine),
        )
        val repository = RemoteLevelRepository(
            levelApi = ktorfit.createLevelApi(),
            serverSettingsRepository = serverSettingsRepository,
        )
    }

    private companion object {

        const val SETTINGS_JSON = """
            {
              "block": "${StubLevel.BLOCK}",
              "idioms": "RARELY",
              "maxWords": ${StubLevel.MAX_WORDS},
              "ratingFrom": ${StubLevel.RANGE_FROM},
              "vocabulary": "${StubLevel.VOCABULARY}"
            }
        """
        const val LEVEL_JSON = """
            {
              "code": "B1",
              "range": {"from": ${StubLevel.RANGE_FROM}, "to": ${StubLevel.RANGE_TO}},
              "settings": $SETTINGS_JSON
            }
        """
        const val EMPTY_LEVEL_JSON = """{"code": "NO_RATING", "range": null, "settings": null}"""

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
