package app.yap.server.core.llm

object ModelCatalog {

    val DEFAULT = ModelChoice(
        model = "deepseek-flash",
        provider = ModelProviderId.DEEPSEEK,
    )

    val providers: List<ModelProviderId> = ModelProviderId.entries

    fun contains(choice: ModelChoice): Boolean = choice.model in choice.provider.models
}
