package app.yap.admin.feature.models.domain.usecase

import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.TaskModel
import app.yap.admin.feature.models.domain.repository.ModelRepository

internal interface SaveModelsUseCase {

    suspend operator fun invoke(tasks: List<TaskModel>): SaveModelsResult
}

internal class DefaultSaveModelsUseCase(
    private val modelRepository: ModelRepository,
) : SaveModelsUseCase {

    override suspend fun invoke(tasks: List<TaskModel>): SaveModelsResult = modelRepository.save(tasks)
}
