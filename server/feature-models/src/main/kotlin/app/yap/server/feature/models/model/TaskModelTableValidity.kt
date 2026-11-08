package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelCatalog

internal object TaskModelTableValidity {

    fun hasEveryTaskOnce(models: List<TaskModel>): Boolean = ModelTask.entries.all { task ->
        models.count { model -> model.task == task } == 1
    }

    fun isInCatalog(models: List<TaskModel>): Boolean = models.all { model -> ModelCatalog.contains(model.choice) }
}
