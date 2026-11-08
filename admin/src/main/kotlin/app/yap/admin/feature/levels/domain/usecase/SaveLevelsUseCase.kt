package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.repository.LevelRepository

internal interface SaveLevelsUseCase {

    suspend operator fun invoke(entries: List<LevelEntry>): SaveLevelsResult
}

internal class DefaultSaveLevelsUseCase(
    private val levelRepository: LevelRepository,
) : SaveLevelsUseCase {

    override suspend fun invoke(entries: List<LevelEntry>): SaveLevelsResult = levelRepository.save(entries)
}
