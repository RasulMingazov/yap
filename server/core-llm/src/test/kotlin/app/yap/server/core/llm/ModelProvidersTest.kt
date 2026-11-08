package app.yap.server.core.llm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.koin.dsl.koinApplication

internal class ModelProvidersTest {

    private val choice = ModelChoice(
        provider = ModelProviderId.DEEPSEEK,
        model = "deepseek-v4-pro",
    )
    private val messages = listOf(StubModelRequest.stubModelMessage())

    @Test
    fun `GIVEN a provider WHEN asking for JSON THEN it gets the messages and the model of the choice`() = runTest {
        val provider = StubModelProvider()
        val providers = ModelProviders(mapOf(ModelProviderId.DEEPSEEK to provider))

        providers.json(choice, messages)

        provider.jsonCall.calledWith(ModelRequest(messages = messages, model = "deepseek-v4-pro"))
    }

    @Test
    fun `GIVEN a provider that answers WHEN asking for JSON THEN the answer comes back as it is`() = runTest {
        val answer = ModelReply.Answered(JsonObject(mapOf("verdict" to JsonPrimitive("accepted"))))
        val providers = ModelProviders(mapOf(ModelProviderId.DEEPSEEK to StubModelProvider(json = answer)))

        assertEquals(expected = answer, actual = providers.json(choice, messages))
    }

    @Test
    fun `GIVEN a provider that fails WHEN asking for JSON THEN the failure comes back as it is`() = runTest {
        val providers = ModelProviders(mapOf(ModelProviderId.DEEPSEEK to StubModelProvider(json = ModelReply.Failed)))

        assertEquals(expected = ModelReply.Failed, actual = providers.json(choice, messages))
    }

    @Test
    fun `GIVEN a provider that is not in the map WHEN asking for JSON THEN it fails`() = runTest {
        assertEquals(expected = ModelReply.Failed, actual = ModelProviders(emptyMap()).json(choice, messages))
    }

    @Test
    fun `GIVEN a module without the DeepSeek key WHEN asking for JSON THEN it fails`() = runTest {
        val koin = koinApplication {
            modules(coreLlmModule(StubLlmConfig.stubLlmConfig(deepSeek = null)))
        }.koin

        assertEquals(expected = ModelReply.Failed, actual = koin.get<ModelProviders>().json(choice, messages))
    }

    @Test
    fun `GIVEN a provider that is closeable WHEN closing THEN it is closed`() {
        var closed = false
        val provider = object : ModelProvider by StubModelProvider(), AutoCloseable {
            override fun close() {
                closed = true
            }
        }

        ModelProviders(mapOf(ModelProviderId.DEEPSEEK to provider)).close()

        assertTrue(closed)
    }
}
