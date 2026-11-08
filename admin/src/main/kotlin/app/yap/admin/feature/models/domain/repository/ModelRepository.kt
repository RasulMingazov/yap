package app.yap.admin.feature.models.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.TaskModel

internal interface ModelRepository {

    suspend fun list(): LoadResult<Models>

    suspend fun save(tasks: List<TaskModel>): SaveModelsResult
}
