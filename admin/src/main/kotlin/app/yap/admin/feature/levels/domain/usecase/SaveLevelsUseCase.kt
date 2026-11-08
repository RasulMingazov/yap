package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.LevelSettings
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.SaveLevelsResult.NumberField
import app.yap.admin.feature.levels.domain.repository.LevelRepository

internal interface SaveLevelsUseCase {

    suspend operator fun invoke(inputs: Map<LevelCode, LevelInput>): SaveLevelsResult
}

internal class DefaultSaveLevelsUseCase(
    private val levelRepository: LevelRepository,
) : SaveLevelsUseCase {

    override suspend fun invoke(inputs: Map<LevelCode, LevelInput>): SaveLevelsResult {
        val readings = inputs.map { (code, input) -> read(code, input) }
        val unread = readings
            .filterIsInstance<Reading.Unread>()
            .firstOrNull()
        if (unread != null) return unread.result
        return levelRepository.save(readings.filterIsInstance<Reading.Read>().map { reading -> reading.entry })
    }

    private fun read(code: LevelCode, input: LevelInput): Reading {
        val idioms = input.idioms
        val maxWords = input.maxWords.toIntOrNull()
        return when {
            input.ratingFrom.isNotBlank() && input.ratingFrom.toIntOrNull() == null ->
                Reading.Unread(SaveLevelsResult.UnreadNumber(code, NumberField.RATING_FROM))
            maxWords == null -> Reading.Unread(SaveLevelsResult.UnreadNumber(code, NumberField.MAX_WORDS))
            idioms == null -> Reading.Unread(SaveLevelsResult.NoIdioms(code))
            else -> Reading.Read(
                LevelEntry(
                    code = code,
                    settings = LevelSettings(
                        block = input.block,
                        idioms = idioms,
                        maxWords = maxWords,
                        ratingFrom = input.ratingFrom.toIntOrNull(),
                        vocabulary = input.vocabulary,
                    ),
                ),
            )
        }
    }

    private sealed interface Reading {

        data class Read(val entry: LevelEntry) : Reading

        data class Unread(val result: SaveLevelsResult) : Reading
    }
}
