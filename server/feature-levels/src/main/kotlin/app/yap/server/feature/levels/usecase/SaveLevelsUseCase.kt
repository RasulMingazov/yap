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
        if (!LevelTableValidity.isValid(entries)) return SaveLevelsOutcome.Invalid
        levelRepository.save(entries)
        return SaveLevelsOutcome.Saved(RatingRanges.levelsOf(entries))
    }
}
