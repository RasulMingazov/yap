package app.yap.admin.feature.models.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.core.network.adminKtorfit
import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.models.data.api.createModelApi
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.StubModels
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

internal class RemoteModelRepositoryTest {

    @Test
    fun `GIVEN a provider with its key setting WHEN loading the models THEN the providers and the tasks are loaded`() = runTest {
        val env = Environment(answer = answer(content = MODELS_JSON))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Loaded(StubModels.stubModels()), actual = result)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN loading the models THEN loading fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN a task code that the app does not know WHEN loading the models THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = MODELS_JSON.replace("TOPIC_REPLY", "TOPIC_JOKE")))

        val result = env.repository.list()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN no saved server settings WHEN loading the models THEN loading fails and no request goes out`() = runTest {
        val env = Environment(answer = answer(content = MODELS_JSON), settings = null)

        val result = env.repository.list()

        assertEquals(expected = listOf(LoadResult.Failed, 0), actual = listOf(result, env.engine.requestHistory.size))
    }

    @Test
    fun `GIVEN the tasks WHEN saving THEN the server gets a PUT with the key and the tasks`() = runTest {
        val env = Environment(answer = answer(content = MODELS_JSON))

        val result = env.repository.save(StubModels.stubTasks())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveModelsResult.Saved(StubModels.stubModels()),
                HttpMethod.Put,
                "${StubServerSettings.ADDRESS}/admin/models",
                "Bearer ${StubServerSettings.KEY}",
                Json.parseToJsonElement(TASKS_JSON),
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
    fun `GIVEN the server rejects the tasks WHEN saving THEN the save is invalid`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.save(StubModels.stubTasks())

        assertEquals(expected = SaveModelsResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN the model list on the server changed WHEN saving THEN the catalog is changed`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.Conflict))

        val result = env.repository.save(StubModels.stubTasks())

        assertEquals(expected = SaveModelsResult.CatalogChanged, actual = result)
    }

    @Test
    fun `GIVEN the server fails WHEN saving THEN the save fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.InternalServerError))

        val result = env.repository.save(StubModels.stubTasks())

        assertEquals(expected = SaveModelsResult.Failed, actual = result)
    }

    private class Environment(
        answer: MockRequestHandler,
        settings: ServerSettings? =StubServerSettings.stubServerSettings(),
    ) {

        val engine = MockEngine(answer)
        private val serverSettingsRepository = StubServerSettingsRepository(settings = settings)
        private val ktorfit = adminKtorfit(
            httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine),
        )
        val repository = RemoteModelRepository(
            modelApi = ktorfit.createModelApi(),
            serverSettingsRepository = serverSettingsRepository,
        )
    }

    private companion object {

        const val TASKS_JSON = """
            [
              {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_REPLY"},
              {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_CHECK"},
              {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_HINTS"},
              {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_REVIEW"},
              {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "CALIBRATION"}
            ]
        """
        const val MODELS_JSON = """
            {
              "providers": [
                {
                  "code": "DEEPSEEK",
                  "connected": true,
                  "keySetting": "DEEPSEEK_API_KEY",
                  "models": ["deepseek-flash", "deepseek-v4-pro"],
                  "name": "DeepSeek"
                }
              ],
              "tasks": $TASKS_JSON
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
