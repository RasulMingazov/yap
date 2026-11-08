package app.yap.server.core.llm

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ProviderConnectionsTest {

    @Test
    fun `GIVEN a DeepSeek key WHEN asking about DeepSeek THEN it is connected`() {
        assertTrue(ProviderConnections(StubLlmConfig.stubLlmConfig()).isConnected(ModelProviderId.DEEPSEEK))
    }

    @Test
    fun `GIVEN no DeepSeek key WHEN asking about DeepSeek THEN it is not connected`() {
        assertFalse(ProviderConnections(StubLlmConfig.stubLlmConfig(deepSeek = null)).isConnected(ModelProviderId.DEEPSEEK))
    }
}
