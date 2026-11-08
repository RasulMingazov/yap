package app.yap.server.core.llm

object ModelCatalog {

    val DEFAULT = ModelChoice(provider = ModelProviderId.DEEPSEEK, model = "deepseek-flash")

    val providers: List<ModelProviderId> = ModelProviderId.entries

    fun contains(choice: ModelChoice): Boolean = choice.model in choice.provider.models
}
