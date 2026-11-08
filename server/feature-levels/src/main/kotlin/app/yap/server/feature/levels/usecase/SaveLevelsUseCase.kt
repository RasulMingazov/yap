package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.LevelTableValidity
import app.yap.server.feature.levels.model.RatingRanges
import app.yap.server.feature.levels.model.SaveLevelsOutcome
import app.yap.server.feature.levels.persistence.LevelRepository

class SaveLevelsUseCase(
    private val levelRepository: LevelRepository,
) {

    suspend operator fun invoke(entries: List<LevelEntry>): SaveLevelsOutcome = when {
        !LevelTableValidity.isValid(entries) -> SaveLevelsOutcome.Invalid
        !LevelTableValidity.isInCatalog(entries) -> SaveLevelsOutcome.ModelUnavailable
        else -> {
            levelRepository.save(entries)
            SaveLevelsOutcome.Saved(RatingRanges.levelsOf(entries))
        }
    }
}
