package app.yap.admin.feature.levels.presentation

import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.LevelSettings

internal fun List<Level>.toInputs(): Map<LevelCode, LevelInput> = associate { level -> level.code to level.toInput() }

private fun Level.toInput(): LevelInput = if (settings == null) EMPTY_INPUT else settings.toInput()

private fun LevelSettings.toInput(): LevelInput = LevelInput(
    block = block,
    idioms = idioms,
    maxWords = maxWords.toString(),
    model = model,
    ratingFrom = ratingFromText(),
    vocabulary = vocabulary,
)

private fun LevelSettings.ratingFromText(): String = ratingFrom
    ?.toString()
    .orEmpty()

private val EMPTY_INPUT = LevelInput(
    block = "",
    idioms = null,
    maxWords = "",
    model = null,
    ratingFrom = "",
    vocabulary = "",
)
