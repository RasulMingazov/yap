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
        code: LevelCode,
        task: ModelTask,
    ): ModelChoice = levelChoice(code, task) ?: TaskModelChoice.of(taskModelRepository.list(), task)

    private suspend fun levelChoice(
        code: LevelCode,
        task: ModelTask,
    ): ModelChoice? = if (task.dependsOnLevel) {
        getLevelsUseCase()
            .find { level -> level.code == code }
            ?.settings
            ?.model
            ?.let(ModelCatalog::choiceOf)
    } else {
        null
    }
}
