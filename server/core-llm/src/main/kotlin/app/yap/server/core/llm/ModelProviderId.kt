package app.yap.server.core.llm

enum class ModelProviderId(
    val title: String,
    val keySetting: String,
    val models: List<String>,
) {
    DEEPSEEK(
        title = "DeepSeek",
        keySetting = "DEEPSEEK_API_KEY",
        models = listOf("deepseek-flash", "deepseek-v4-pro"),
    ),
}
