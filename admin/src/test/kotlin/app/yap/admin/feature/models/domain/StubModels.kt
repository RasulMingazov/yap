package app.yap.admin.feature.models.domain

internal object StubModels {

    const val MODEL = "deepseek-flash"
    const val PROVIDER = "DEEPSEEK"

    fun stubChoice(
        model: String = MODEL,
        provider: String = PROVIDER,
    ): ModelChoice = ModelChoice(
        model = model,
        provider = provider,
    )

    fun stubModels(
        providers: List<Provider> = listOf(stubProvider()),
        tasks: List<TaskModel> = stubTasks(),
    ): Models = Models(
        providers = providers,
        tasks = tasks,
    )

    fun stubProvider(
        code: String = PROVIDER,
        connected: Boolean = true,
        keySetting: String = "DEEPSEEK_API_KEY",
        models: List<String> = listOf(MODEL, "deepseek-v4-pro"),
        name: String = "DeepSeek",
    ): Provider = Provider(
        code = code,
        connected = connected,
        keySetting = keySetting,
        models = models,
        name = name,
    )

    fun stubTask(
        choice: ModelChoice = stubChoice(),
        task: ModelTask = ModelTask.TOPIC_REPLY,
    ): TaskModel = TaskModel(
        choice = choice,
        task = task,
    )

    fun stubTasks(): List<TaskModel> = ModelTask.entries.map { task -> stubTask(task = task) }
}
