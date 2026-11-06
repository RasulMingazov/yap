package app.yap.server.core.llm

import io.github.rasulmingazov.stubcall.StubCall1
import kotlinx.serialization.json.JsonObject

class StubModelProvider(
    json: ModelReply<JsonObject> = ModelReply.Answered(JsonObject(emptyMap())),
    text: ModelReply<String> = ModelReply.Answered("Hello!"),
) : ModelProvider {

    val jsonCall = StubCall1.returns<ModelRequest, ModelReply<JsonObject>>(json)
    val textCall = StubCall1.returns<ModelRequest, ModelReply<String>>(text)

    override suspend fun json(request: ModelRequest): ModelReply<JsonObject> = jsonCall.invoke(request)

    override suspend fun text(request: ModelRequest): ModelReply<String> = textCall.invoke(request)
}
