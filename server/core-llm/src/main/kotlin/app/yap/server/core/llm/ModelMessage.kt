package app.yap.server.core.llm

data class ModelMessage(
    val role: ModelRole,
    val text: String,
)
