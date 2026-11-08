package app.yap.server.core.llm

enum class ModelProviderId(
    val keySetting: String,
    val models: List<String>,
    val title: String,
) {
    DEEPSEEK(
        keySetting = "DEEPSEEK_API_KEY",
        models = listOf("deepseek-flash", "deepseek-v4-pro"),
        title = "DeepSeek",
    ),
}
