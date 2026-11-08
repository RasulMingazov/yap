package app.yap.server.core.llm

import kotlin.test.Test
import kotlin.test.assertTrue

internal class ProviderConnectionsTest {

    @Test
    fun `GIVEN a DeepSeek key WHEN asking about DeepSeek THEN it is connected`() {
        val connections = ProviderConnections(config = StubLlmConfig.stubLlmConfig())

        assertTrue(connections.isConnected(ModelProviderId.DEEPSEEK))
    }
}
