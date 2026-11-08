package app.yap.server.core.llm

object ModelCatalog {

    val DEFAULT = ModelChoice(
        model = "deepseek-flash",
        provider = ModelProviderId.DEEPSEEK,
    )

    val providers: List<ModelProviderId> = ModelProviderId.entries

    fun contains(choice: ModelChoice): Boolean = choice.model in choice.provider.models

    fun choiceOf(model: String): ModelChoice? = providers
        .find { provider -> model in provider.models }
        ?.let { provider ->
            ModelChoice(
                model = model,
                provider = provider,
            )
        }
}
