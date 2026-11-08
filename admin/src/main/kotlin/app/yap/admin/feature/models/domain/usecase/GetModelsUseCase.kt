package app.yap.admin.feature.models.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.repository.ModelRepository

internal interface GetModelsUseCase {

    suspend operator fun invoke(): LoadResult<Models>
}

internal class DefaultGetModelsUseCase(
    private val modelRepository: ModelRepository,
) : GetModelsUseCase {

    override suspend fun invoke(): LoadResult<Models> = modelRepository.list()
}
