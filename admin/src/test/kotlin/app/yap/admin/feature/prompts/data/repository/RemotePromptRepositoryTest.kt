package app.yap.admin.feature.prompts.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.core.network.adminKtorfit
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.prompts.data.api.createPromptApi
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.StubPrompt
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

internal class RemotePromptRepositoryTest {

    @Test
    fun `GIVEN the server lists the prompts WHEN loading them THEN they are loaded`() = runTest {
        val env = Environment(answer = answer(content = """[{"group": "TOPIC", "promptKey": "TOPIC_REPLY"}]"""))

        val result = env.repository.list()

        val summary = PromptSummary(group = PromptGroup.TOPIC, promptKey = PromptKey.TOPIC_REPLY)
        assertEquals(expected = LoadResult.Loaded(listOf(summary)), actual = result)
    }

    @Test
    fun `GIVEN the server returns a prompt WHEN loading it THEN it is loaded from its address`() = runTest {
        val env = Environment(answer = answer(content = PROMPT_JSON))

        val result = env.repository.find(PromptKey.TOPIC_REPLY)

        assertEquals(
            expected = LoadResult.Loaded(StubPrompt.stubPrompt()) to "$PROMPTS_URL/TOPIC_REPLY",
            actual = result to env.engine.requestHistory.single().url.toString(),
        )
    }

    @Test
    fun `GIVEN a text WHEN saving a prompt THEN the server gets the text with the key`() = runTest {
        val env = Environment(answer = answer(content = PROMPT_JSON, status = HttpStatusCode.Created))

        val result = env.repository.save(promptKey = PromptKey.TOPIC_REPLY, text = StubPrompt.TEXT)

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SavePromptResult.Saved(StubPrompt.stubPrompt()),
                HttpMethod.Post,
                "$PROMPTS_URL/TOPIC_REPLY/versions",
                "Bearer ${StubServerSettings.KEY}",
                Json.parseToJsonElement("""{"text": "${StubPrompt.TEXT}"}"""),
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
    fun `GIVEN the server rejects the text WHEN saving a prompt THEN the text is empty`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.save(promptKey = PromptKey.TOPIC_REPLY, text = " ")

        assertEquals(expected = SavePromptResult.EmptyText, actual = result)
    }

    @Test
    fun `GIVEN a server error WHEN saving a prompt THEN saving fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.InternalServerError))

        val result = env.repository.save(promptKey = PromptKey.TOPIC_REPLY, text = StubPrompt.TEXT)

        assertEquals(expected = SavePromptResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN saving a prompt THEN saving fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.save(promptKey = PromptKey.TOPIC_REPLY, text = StubPrompt.TEXT)

        assertEquals(expected = SavePromptResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN a saved version WHEN restoring it THEN the server gets a POST at its address`() = runTest {
        val env = Environment(answer = answer(content = PROMPT_JSON))

        val result = env.repository.restore(promptKey = PromptKey.TOPIC_REPLY, number = StubPrompt.NUMBER)

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                RestorePromptResult.Restored(StubPrompt.stubPrompt()),
                HttpMethod.Post,
                "$PROMPTS_URL/TOPIC_REPLY/versions/${StubPrompt.NUMBER}/restore",
            ),
            actual = listOf(result, request.method, request.url.toString()),
        )
    }

    @Test
    fun `GIVEN an unknown version WHEN restoring it THEN restoring fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.NotFound))

        val result = env.repository.restore(promptKey = PromptKey.TOPIC_REPLY, number = StubPrompt.NUMBER)

        assertEquals(expected = RestorePromptResult.Failed, actual = result)
    }

    private class Environment(answer: MockRequestHandler) {

        val engine = MockEngine(answer)
        val serverSettingsRepository = StubServerSettingsRepository(settings = StubServerSettings.stubServerSettings())
        private val ktorfit = adminKtorfit(
            httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine),
        )
        val repository = RemotePromptRepository(
            promptApi = ktorfit.createPromptApi(),
            serverSettingsRepository = serverSettingsRepository,
        )
    }

    private companion object {

        const val PROMPTS_URL = "${StubServerSettings.ADDRESS}/admin/prompts"
        const val PROMPT_JSON = """{"group": "TOPIC", "promptKey": "TOPIC_REPLY", "text": "${StubPrompt.TEXT}",
            "variables": ["${StubPrompt.VARIABLE}"],
            "versions": [{"isCurrent": true, "number": ${StubPrompt.NUMBER}, "savedAt": "${StubPrompt.SAVED_AT}"}]}"""

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
