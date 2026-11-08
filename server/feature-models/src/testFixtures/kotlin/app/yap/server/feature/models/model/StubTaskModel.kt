package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.core.llm.ModelChoice

object StubTaskModel {

    const val PRO_MODEL = "deepseek-v4-pro"

    fun stubTaskModel(
        choice: ModelChoice = ModelCatalog.DEFAULT,
        task: ModelTask = ModelTask.TOPIC_REPLY,
    ): TaskModel = TaskModel(
        choice = choice,
        task = task,
    )

    fun stubTable(
        review: ModelChoice = ModelCatalog.DEFAULT,
    ): List<TaskModel> = ModelTask.entries.map { task ->
        stubTaskModel(
            choice = if (task == ModelTask.TOPIC_REVIEW) review else ModelCatalog.DEFAULT,
            task = task,
        )
    }

    fun stubChoice(
        model: String = PRO_MODEL,
    ): ModelChoice = ModelChoice(
        model = model,
        provider = ModelCatalog.DEFAULT.provider,
    )
}
