package app.yap.server.core.llm.deepseek

import app.yap.server.core.llm.ModelReply
import app.yap.server.core.llm.StubModelRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class DeepSeekModelProviderTest {

    @Test
    fun `GIVEN a request WHEN asking for text THEN DeepSeek gets the key, the model and the messages`() = runTest {
        val env = Environment(answer = answer(content = "Hello!"), scheduler = testScheduler)

        env.provider.text(StubModelRequest.stubModelRequest())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = "Bearer $API_KEY" to Json.parseToJsonElement(
                """{"messages": [{"content": "Hi", "role": "user"}], "model": "deepseek-flash"}""",
            ),
            actual = request.headers[HttpHeaders.Authorization] to
                Json.parseToJsonElement(request.body.toByteArray().decodeToString()),
        )
    }

    @Test
    fun `GIVEN a request WHEN asking for JSON THEN DeepSeek gets the JSON format`() = runTest {
        val env = Environment(answer = answer(content = """{"passed":true}"""), scheduler = testScheduler)

        env.provider.json(StubModelRequest.stubModelRequest())

        assertEquals(
            expected = Json.parseToJsonElement(
                """
                {
                  "messages": [{"content": "Hi", "role": "user"}],
                  "model": "deepseek-flash",
                  "response_format": {"type": "json_object"}
                }
                """,
            ),
            actual = Json.parseToJsonElement(env.engine.requestHistory.single().body.toByteArray().decodeToString()),
        )
    }

    @Test
    fun `GIVEN DeepSeek answers with a JSON object WHEN asking for JSON THEN the object comes back`() = runTest {
        val env = Environment(answer = answer(content = """{"passed":true}"""), scheduler = testScheduler)

        val reply = env.provider.json(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Answered(buildJsonObject { put("passed", true) }), actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek answers with plain text WHEN asking for JSON THEN the call fails`() = runTest {
        val env = Environment(answer = answer(content = "Sure, here it is"), scheduler = testScheduler)

        val reply = env.provider.json(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Failed, actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek answers WHEN asking for text THEN the text comes back`() = runTest {
        val env = Environment(answer = answer(content = "Hello!"), scheduler = testScheduler)

        val reply = env.provider.text(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Answered("Hello!"), actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek answers with spaces only WHEN asking for text THEN the call fails`() = runTest {
        val env = Environment(answer = answer(content = "   "), scheduler = testScheduler)

        val reply = env.provider.text(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Failed, actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek stops at the token limit WHEN asking for text THEN the call fails`() = runTest {
        val env = Environment(answer = answer(content = "Hello, I am", finishReason = "length"), scheduler = testScheduler)

        val reply = env.provider.text(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Failed, actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek rejects the key WHEN asking for text THEN the call fails`() = runTest {
        val env = Environment(
            answer = { respond(content = """{"error":{"message":"Authentication Fails"}}""", status = HttpStatusCode.Unauthorized) },
            scheduler = testScheduler,
        )

        val reply = env.provider.text(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Failed, actual = reply)
    }

    @Test
    fun `GIVEN DeepSeek does not answer WHEN a minute passes THEN the call fails`() = runTest {
        val env = Environment(answer = { awaitCancellation() }, scheduler = testScheduler)

        val reply = env.provider.text(StubModelRequest.stubModelRequest())

        assertEquals(expected = ModelReply.Failed to 60_000L, actual = reply to testScheduler.currentTime)
    }

    private class Environment(
        answer: MockRequestHandler,
        scheduler: TestCoroutineScheduler,
    ) {

        val engine = MockEngine(
            MockEngineConfig().apply {
                addHandler(answer)
                dispatcher = StandardTestDispatcher(scheduler)
            },
        )
        val provider = DeepSeekModelProvider(
            config = DeepSeekConfig(apiKey = API_KEY),
            engine = engine,
        )
    }

    private companion object {

        const val API_KEY = "test-deepseek-key"

        fun answer(content: String, finishReason: String = "stop"): MockRequestHandler = {
            respond(
                content = """
                    {"choices": [{"finish_reason": "$finishReason", "message": {"content": ${JsonPrimitive(content)}}}]}
                """,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
