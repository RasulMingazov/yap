package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.repository.LevelRepository

internal interface GetLevelsUseCase {

    suspend operator fun invoke(): LoadResult<List<Level>>
}

internal class DefaultGetLevelsUseCase(
    private val levelRepository: LevelRepository,
) : GetLevelsUseCase {

    override suspend fun invoke(): LoadResult<List<Level>> = levelRepository.list()
}
