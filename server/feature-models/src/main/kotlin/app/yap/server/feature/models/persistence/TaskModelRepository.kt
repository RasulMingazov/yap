package app.yap.server.feature.models.persistence

import app.yap.server.feature.models.model.TaskModel

interface TaskModelRepository {

    suspend fun list(): List<TaskModel>

    suspend fun save(models: List<TaskModel>)
}
