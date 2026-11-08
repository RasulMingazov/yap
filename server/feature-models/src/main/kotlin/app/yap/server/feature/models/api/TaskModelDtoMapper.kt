package app.yap.server.feature.models.api

import app.yap.contract.admin.ModelsDto
import app.yap.contract.admin.ProviderDto
import app.yap.contract.admin.TaskModelDto
import app.yap.server.core.llm.ModelChoice
import app.yap.server.core.llm.ModelProviderId
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.ModelsOverview
import app.yap.server.feature.models.model.ProviderState
import app.yap.server.feature.models.model.TaskModel
import io.ktor.server.plugins.BadRequestException

internal fun ModelsOverview.toDto(): ModelsDto = ModelsDto(
    providers = providers.map { provider -> provider.toDto() },
    tasks = tasks.map { model -> model.toDto() },
)

private fun ProviderState.toDto(): ProviderDto = ProviderDto(
    code = id.name,
    connected = connected,
    keySetting = id.keySetting,
    models = id.models,
    name = id.title,
)

private fun TaskModel.toDto(): TaskModelDto = TaskModelDto(
    model = choice.model,
    provider = choice.provider.name,
    task = task.name,
)

internal fun List<TaskModelDto>.toModels(): List<TaskModel>? = mapNotNull { dto -> dto.toModel() }
    .takeIf { models -> models.size == size }

private fun TaskModelDto.toModel(): TaskModel? {
    val modelTask = task.toTask()
    return provider.toProvider()?.let { providerId ->
        TaskModel(
            choice = ModelChoice(
                model = model,
                provider = providerId,
            ),
            task = modelTask,
        )
    }
}

private fun String.toTask(): ModelTask = ModelTask.entries.find { modelTask -> modelTask.name == this }
    ?: throw BadRequestException("The task is unknown")

private fun String.toProvider(): ModelProviderId? = ModelProviderId.entries.find { providerId -> providerId.name == this }
