package app.yap.server.core.llm

interface ModelProvider {

    suspend fun text(request: ModelRequest): ModelReply<String>
}
