package app.yap.server.core.llm

import app.yap.server.core.llm.deepseek.DeepSeekConfig

data class LlmConfig(
    val deepSeek: DeepSeekConfig?,
)
