package app.yap.server.feature.models.usecase

import app.yap.server.feature.models.model.SaveTaskModelsOutcome
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.model.TaskModelTableValidity
import app.yap.server.feature.models.persistence.TaskModelRepository

class SaveModelsUseCase(
    private val getModelsUseCase: GetModelsUseCase,
    private val taskModelRepository: TaskModelRepository,
) {

    suspend operator fun invoke(models: List<TaskModel>): SaveTaskModelsOutcome = when {
        !TaskModelTableValidity.hasEveryTaskOnce(models) -> SaveTaskModelsOutcome.Invalid
        !TaskModelTableValidity.isInCatalog(models) -> SaveTaskModelsOutcome.CatalogChanged
        else -> {
            taskModelRepository.save(models)
            SaveTaskModelsOutcome.Saved(getModelsUseCase())
        }
    }
}
