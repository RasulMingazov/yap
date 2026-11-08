package app.yap.server.feature.models.usecase

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.core.llm.ProviderConnections
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.ModelsOverview
import app.yap.server.feature.models.model.ProviderState
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.model.TaskModelChoice
import app.yap.server.feature.models.persistence.TaskModelRepository

class GetModelsUseCase(
    private val providerConnections: ProviderConnections,
    private val taskModelRepository: TaskModelRepository,
) {

    suspend operator fun invoke(): ModelsOverview {
        val stored = taskModelRepository.list()
        return ModelsOverview(
            providers = ModelCatalog.providers.map { provider ->
                ProviderState(
                    connected = providerConnections.isConnected(provider),
                    id = provider,
                )
            },
            tasks = ModelTask.entries.map { task ->
                TaskModel(
                    choice = TaskModelChoice.of(stored, task),
                    task = task,
                )
            },
        )
    }
}
