package app.yap.server.core.llm

data class ModelRequest(
    val messages: List<ModelMessage>,
    val model: String,
)
