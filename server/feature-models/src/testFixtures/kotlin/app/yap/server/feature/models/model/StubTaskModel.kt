package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.core.llm.ModelChoice
import app.yap.server.core.llm.ModelProviderId

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
        calibration: ModelChoice = ModelCatalog.DEFAULT,
        check: ModelChoice = ModelCatalog.DEFAULT,
        hints: ModelChoice = ModelCatalog.DEFAULT,
        reply: ModelChoice = ModelCatalog.DEFAULT,
        review: ModelChoice = ModelCatalog.DEFAULT,
    ): List<TaskModel> = listOf(
        stubTaskModel(choice = reply, task = ModelTask.TOPIC_REPLY),
        stubTaskModel(choice = check, task = ModelTask.TOPIC_CHECK),
        stubTaskModel(choice = hints, task = ModelTask.TOPIC_HINTS),
        stubTaskModel(choice = review, task = ModelTask.TOPIC_REVIEW),
        stubTaskModel(choice = calibration, task = ModelTask.CALIBRATION),
    )

    fun stubChoice(
        model: String = PRO_MODEL,
        provider: ModelProviderId = ModelProviderId.DEEPSEEK,
    ): ModelChoice = ModelChoice(
        model = model,
        provider = provider,
    )
}
