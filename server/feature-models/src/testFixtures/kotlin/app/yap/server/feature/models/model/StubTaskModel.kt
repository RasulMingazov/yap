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
    ): List<TaskModel> = listOf(
        stubTaskModel(task = ModelTask.TOPIC_REPLY),
        stubTaskModel(task = ModelTask.TOPIC_CHECK),
        stubTaskModel(task = ModelTask.TOPIC_HINTS),
        stubTaskModel(choice = review, task = ModelTask.TOPIC_REVIEW),
        stubTaskModel(task = ModelTask.CALIBRATION),
    )

    fun stubChoice(
        model: String = PRO_MODEL,
    ): ModelChoice = ModelChoice(
        model = model,
        provider = ModelCatalog.DEFAULT.provider,
    )
}
