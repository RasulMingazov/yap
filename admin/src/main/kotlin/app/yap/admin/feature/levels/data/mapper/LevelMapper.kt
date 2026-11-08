package app.yap.admin.feature.levels.data.mapper

import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.LevelSettings
import app.yap.admin.feature.levels.domain.RatingRange
import app.yap.contract.admin.LevelDto
import app.yap.contract.admin.LevelInputDto
import app.yap.contract.admin.LevelSettingsDto
import app.yap.contract.admin.RatingRangeDto

internal fun LevelDto.toDomain(): Level = Level(
    code = LevelCode.valueOf(code),
    range = range?.toDomain(),
    settings = settings?.toDomain(),
)

private fun RatingRangeDto.toDomain(): RatingRange = RatingRange(
    from = from,
    to = to,
)

private fun LevelSettingsDto.toDomain(): LevelSettings = LevelSettings(
    block = block,
    idioms = Idioms.valueOf(idioms),
    maxWords = maxWords,
    model = model,
    ratingFrom = ratingFrom,
    vocabulary = vocabulary,
)

internal fun LevelEntry.toDto(): LevelInputDto = LevelInputDto(
    code = code.name,
    settings = settings.toDto(),
)

private fun LevelSettings.toDto(): LevelSettingsDto = LevelSettingsDto(
    block = block,
    idioms = idioms.name,
    maxWords = maxWords,
    model = model,
    ratingFrom = ratingFrom,
    vocabulary = vocabulary,
)
