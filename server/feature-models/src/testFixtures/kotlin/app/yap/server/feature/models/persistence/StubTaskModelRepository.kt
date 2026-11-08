package app.yap.server.feature.models.persistence

import app.yap.server.feature.models.model.StubTaskModel
import app.yap.server.feature.models.model.TaskModel
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

class StubTaskModelRepository(
    models: List<TaskModel> = StubTaskModel.stubTable(),
) : TaskModelRepository {

    val listCall = StubCall0.returns<List<TaskModel>>(models)
    val saveCall = StubCall1.unit<List<TaskModel>>()

    override suspend fun list(): List<TaskModel> = listCall.invoke()

    override suspend fun save(models: List<TaskModel>) = saveCall.invoke(models)
}
