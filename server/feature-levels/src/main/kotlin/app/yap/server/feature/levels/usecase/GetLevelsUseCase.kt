package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.Level
import app.yap.server.feature.levels.model.RatingRanges
import app.yap.server.feature.levels.persistence.LevelRepository

class GetLevelsUseCase(
    private val levelRepository: LevelRepository,
) {

    suspend operator fun invoke(): List<Level> = RatingRanges.levelsOf(levelRepository.list())
}
