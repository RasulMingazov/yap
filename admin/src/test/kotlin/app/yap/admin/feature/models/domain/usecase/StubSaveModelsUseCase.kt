package app.yap.admin.feature.models.domain.usecase

import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.StubModels
import app.yap.admin.feature.models.domain.TaskModel
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubSaveModelsUseCase(
    result: SaveModelsResult = SaveModelsResult.Saved(StubModels.stubModels()),
) : SaveModelsUseCase {

    val invokeCall = StubCall1.returns<List<TaskModel>, SaveModelsResult>(result)

    override suspend fun invoke(tasks: List<TaskModel>): SaveModelsResult = invokeCall.invoke(tasks)
}
