package app.yap.server.core.llm.deepseek

import app.yap.server.core.llm.ModelReply
import app.yap.server.core.llm.StubModelRequest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assumptions.assumeTrue

internal class DeepSeekModelProviderLiveTest {

    @Test
    fun `GIVEN a DeepSeek key WHEN asking DeepSeek for text THEN it answers`() = runTest(timeout = 2.minutes) {
        val apiKey = System.getenv("DEEPSEEK_API_KEY").orEmpty()
        assumeTrue(apiKey.isNotBlank(), "DEEPSEEK_API_KEY is not set: the live DeepSeek test did not run")
        val provider = DeepSeekModelProvider(DeepSeekConfig(apiKey = apiKey))

        val reply = provider.text(
            StubModelRequest.stubModelRequest(
                messages = listOf(StubModelRequest.stubModelMessage(text = "Say hello in one word.")),
            ),
        )

        assertIs<ModelReply.Answered<*>>(reply)
    }
}
