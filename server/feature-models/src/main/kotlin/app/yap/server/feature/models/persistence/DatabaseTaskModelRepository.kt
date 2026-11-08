package app.yap.server.feature.models.persistence

import app.yap.server.core.database.Database
import app.yap.server.core.llm.ModelChoice
import app.yap.server.core.llm.ModelProviderId
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.TaskModel
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert

class DatabaseTaskModelRepository(
    private val database: Database,
) : TaskModelRepository {

    override suspend fun list(): List<TaskModel> = database.transaction {
        TaskModelsTable
            .selectAll()
            .mapNotNull { row -> row.toTaskModel() }
    }

    override suspend fun save(models: List<TaskModel>) {
        database.transaction {
            models.forEach { model ->
                TaskModelsTable.upsert { statement -> statement.write(model) }
            }
        }
    }
}

private fun ResultRow.toTaskModel(): TaskModel? {
    val provider = ModelProviderId.entries.find { id -> id.name == this[TaskModelsTable.provider] }
    val task = ModelTask.entries.find { entry -> entry.name == this[TaskModelsTable.task] }
    return if (provider == null || task == null) {
        null
    } else {
        TaskModel(
            choice = ModelChoice(
                model = this[TaskModelsTable.model],
                provider = provider,
            ),
            task = task,
        )
    }
}

private fun UpdateBuilder<*>.write(model: TaskModel) {
    this[TaskModelsTable.model] = model.choice.model
    this[TaskModelsTable.provider] = model.choice.provider.name
    this[TaskModelsTable.task] = model.task.name
}
