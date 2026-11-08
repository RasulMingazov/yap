package app.yap.admin.feature.models.domain

internal object StubModels {

    const val MODEL = "deepseek-flash"
    const val PROVIDER = "DEEPSEEK"

    fun stubModels(): Models = Models(
        providers = listOf(stubProvider()),
        tasks = stubTasks(),
    )

    fun stubProvider(): Provider = Provider(
        code = PROVIDER,
        connected = true,
        keySetting = "DEEPSEEK_API_KEY",
        models = listOf(MODEL, "deepseek-v4-pro"),
        name = "DeepSeek",
    )

    fun stubTasks(): List<TaskModel> = ModelTask.entries.map { task ->
        TaskModel(
            choice = ModelChoice(
                model = MODEL,
                provider = PROVIDER,
            ),
            task = task,
        )
    }
}
