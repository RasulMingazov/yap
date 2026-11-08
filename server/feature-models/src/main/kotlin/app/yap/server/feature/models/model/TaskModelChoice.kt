package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.core.llm.ModelChoice

internal object TaskModelChoice {

    fun of(stored: List<TaskModel>, task: ModelTask): ModelChoice = stored
        .find { model -> model.task == task }
        ?.choice
        ?.takeIf(ModelCatalog::contains)
        ?: ModelCatalog.DEFAULT
}
