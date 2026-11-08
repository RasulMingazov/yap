package app.yap.admin.feature.levels.domain

internal data class Level(
    val code: LevelCode,
    val range: RatingRange?,
    val settings: LevelSettings?,
)

internal fun List<Level>.hasEveryLevel(): Boolean = map { level -> level.code } == LevelCode.entries
