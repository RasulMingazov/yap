package app.yap.server.core.llm.deepseek

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class DeepSeekConfig(
    val apiKey: String,
    val timeout: Duration = DEFAULT_TIMEOUT,
) {

    init {
        require(apiKey.isNotBlank()) { "DEEPSEEK_API_KEY must not be blank" }
    }

    private companion object {

        val DEFAULT_TIMEOUT = 60.seconds
    }
}
