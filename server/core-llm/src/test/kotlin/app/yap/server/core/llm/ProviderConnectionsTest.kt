package app.yap.server.core.llm

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ProviderConnectionsTest {

    @Test
    fun `GIVEN a DeepSeek key WHEN asking about DeepSeek THEN it is connected`() {
        val connections = ProviderConnections(config = StubLlmConfig.stubLlmConfig())

        assertTrue(connections.isConnected(ModelProviderId.DEEPSEEK))
    }

    @Test
    fun `GIVEN no DeepSeek key WHEN asking about DeepSeek THEN it is not connected`() {
        val connections = ProviderConnections(config = StubLlmConfig.stubLlmConfig(deepSeek = null))

        assertFalse(connections.isConnected(ModelProviderId.DEEPSEEK))
    }
}
