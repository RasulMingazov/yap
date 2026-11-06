package app.yap.server.core.llm.deepseek

import app.yap.server.core.llm.ModelReply
import app.yap.server.core.llm.StubModelRequest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue

internal class DeepSeekModelProviderLiveTest {

    @Test
    fun `GIVEN a DeepSeek key WHEN asking DeepSeek for text THEN it answers`() = runBlocking<Unit> {
        val apiKey = System.getenv("DEEPSEEK_API_KEY").orEmpty()
        assumeTrue(apiKey.isNotBlank(), "DEEPSEEK_API_KEY is not set: the live DeepSeek test did not run")
        val request = StubModelRequest.stubModelRequest(
            messages = listOf(StubModelRequest.stubModelMessage(text = "Say hello in one word.")),
        )

        val reply = DeepSeekModelProvider(DeepSeekConfig(apiKey = apiKey)).use { provider -> provider.text(request) }

        assertIs<ModelReply.Answered<*>>(reply)
    }
}
