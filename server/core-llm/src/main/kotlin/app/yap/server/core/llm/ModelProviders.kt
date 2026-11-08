package app.yap.server.core.llm

import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ModelProviders::class.java)

class ModelProviders(
    private val providers: Map<ModelProviderId, ModelProvider>,
) : AutoCloseable {

    suspend fun json(choice: ModelChoice, messages: List<ModelMessage>): ModelReply<JsonObject> {
        val provider = providers[choice.provider]
        if (provider == null) {
            logger.warn("No connection to {}", choice.provider)
            return ModelReply.Failed
        }
        return provider.json(ModelRequest(messages = messages, model = choice.model))
    }

    override fun close() {
        providers.values.filterIsInstance<AutoCloseable>().forEach(AutoCloseable::close)
    }
}
