package app.yap.server.feature.levels.model

data class Level(
    val code: LevelCode,
    val range: RatingRange?,
    val settings: LevelSettings?,
)
