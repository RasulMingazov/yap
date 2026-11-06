package app.yap.server.core.llm

import kotlinx.serialization.json.JsonObject

interface ModelProvider {

    suspend fun json(request: ModelRequest): ModelReply<JsonObject>

    suspend fun text(request: ModelRequest): ModelReply<String>
}
