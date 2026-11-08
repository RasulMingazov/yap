package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.LevelTableValidity
import app.yap.server.feature.levels.model.RatingRanges
import app.yap.server.feature.levels.model.SaveLevelsOutcome
import app.yap.server.feature.levels.persistence.LevelRepository

class SaveLevelsUseCase(
    private val levelRepository: LevelRepository,
) {

    suspend operator fun invoke(entries: List<LevelEntry>): SaveLevelsOutcome {
        val normalized = entries.map { entry -> entry.normalized() }
        return when {
            !LevelTableValidity.isValid(normalized) -> SaveLevelsOutcome.Invalid
            !LevelTableValidity.isInCatalog(normalized) -> SaveLevelsOutcome.ModelUnavailable
            else -> {
                levelRepository.save(normalized)
                SaveLevelsOutcome.Saved(RatingRanges.levelsOf(normalized))
            }
        }
    }
}
