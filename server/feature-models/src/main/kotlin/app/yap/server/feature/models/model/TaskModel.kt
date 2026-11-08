package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelChoice

data class TaskModel(
    val choice: ModelChoice,
    val task: ModelTask,
)
