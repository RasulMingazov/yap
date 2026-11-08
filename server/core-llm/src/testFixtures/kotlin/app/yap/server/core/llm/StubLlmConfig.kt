package app.yap.server.core.llm

import app.yap.server.core.llm.deepseek.DeepSeekConfig

object StubLlmConfig {

    const val API_KEY = "test-api-key"

    fun stubLlmConfig(
        deepSeek: DeepSeekConfig? = DeepSeekConfig(apiKey = API_KEY),
    ): LlmConfig = LlmConfig(
        deepSeek = deepSeek,
    )
}
