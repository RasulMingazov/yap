package app.yap.server.feature.levels.api

import app.yap.contract.admin.LevelDto
import app.yap.contract.admin.LevelInputDto
import app.yap.contract.admin.LevelSettingsDto
import app.yap.contract.admin.RatingRangeDto
import app.yap.server.feature.levels.model.Idioms
import app.yap.server.feature.levels.model.Level
import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.LevelSettings
import app.yap.server.feature.levels.model.RatingRange
import io.ktor.server.plugins.BadRequestException

internal fun Level.toDto(): LevelDto = LevelDto(
    code = code.name,
    range = range?.toDto(),
    settings = settings?.toDto(),
)

private fun RatingRange.toDto(): RatingRangeDto = RatingRangeDto(
    from = from,
    to = to,
)

private fun LevelSettings.toDto(): LevelSettingsDto = LevelSettingsDto(
    block = block,
    idioms = idioms.name,
    maxWords = maxWords,
    model = model,
    ratingFrom = ratingFrom,
    vocabulary = vocabulary,
)

internal fun LevelInputDto.toModel(): LevelEntry = LevelEntry(
    code = code.toLevelCode(),
    settings = settings.toModel(),
)

private fun String.toLevelCode(): LevelCode = LevelCode.entries.find { levelCode -> levelCode.name == this }
    ?: throw BadRequestException("The level code is unknown")

private fun LevelSettingsDto.toModel(): LevelSettings = LevelSettings(
    block = block.trim(),
    idioms = idioms.toIdioms(),
    maxWords = maxWords,
    model = model,
    ratingFrom = ratingFrom,
    vocabulary = vocabulary.trim(),
)

private fun String.toIdioms(): Idioms = Idioms.entries.find { setting -> setting.name == this }
    ?: throw BadRequestException("The idioms setting is unknown")
