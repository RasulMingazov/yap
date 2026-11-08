package app.yap.server.core.llm

data class ModelChoice(
    val provider: ModelProviderId,
    val model: String,
)
