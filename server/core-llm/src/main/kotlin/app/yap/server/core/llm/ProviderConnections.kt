package app.yap.server.core.llm

class ProviderConnections(
    private val config: LlmConfig,
) {

    fun isConnected(provider: ModelProviderId): Boolean = when (provider) {
        ModelProviderId.DEEPSEEK -> config.deepSeek != null
    }
}
