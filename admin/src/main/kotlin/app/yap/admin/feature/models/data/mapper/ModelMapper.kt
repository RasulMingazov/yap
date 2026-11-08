package app.yap.admin.feature.models.data.mapper

import app.yap.admin.feature.models.domain.ModelChoice
import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.Provider
import app.yap.admin.feature.models.domain.TaskModel
import app.yap.contract.admin.ModelsDto
import app.yap.contract.admin.ProviderDto
import app.yap.contract.admin.TaskModelDto

internal fun ModelsDto.toDomain(): Models = Models(
    providers = providers.map { provider -> provider.toDomain() },
    tasks = tasks.map { task -> task.toDomain() },
)

private fun ProviderDto.toDomain(): Provider = Provider(
    code = code,
    connected = connected,
    keySetting = keySetting,
    models = models,
    name = name,
)

private fun TaskModelDto.toDomain(): TaskModel = TaskModel(
    choice = ModelChoice(
        model = model,
        provider = provider,
    ),
    task = ModelTask.valueOf(task),
)

internal fun TaskModel.toDto(): TaskModelDto = TaskModelDto(
    model = choice.model,
    provider = choice.provider,
    task = task.name,
)
