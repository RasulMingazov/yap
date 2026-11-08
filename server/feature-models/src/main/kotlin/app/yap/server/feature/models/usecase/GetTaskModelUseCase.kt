package app.yap.server.feature.models.usecase

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.core.llm.ModelChoice
import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.usecase.GetLevelsUseCase
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.TaskModelChoice
import app.yap.server.feature.models.persistence.TaskModelRepository

class GetTaskModelUseCase(
    private val getLevelsUseCase: GetLevelsUseCase,
    private val taskModelRepository: TaskModelRepository,
) {

    suspend operator fun invoke(
        level: LevelCode,
        task: ModelTask,
    ): ModelChoice = levelChoice(level, task) ?: TaskModelChoice.of(taskModelRepository.list(), task)

    private suspend fun levelChoice(
        level: LevelCode,
        task: ModelTask,
    ): ModelChoice? = if (task.dependsOnLevel) {
        getLevelsUseCase()
            .find { entry -> entry.code == level }
            ?.settings
            ?.model
            ?.let(ModelCatalog::choiceOf)
    } else {
        null
    }
}
