package app.yap.server.feature.levels.model

data class LevelEntry(
    val code: LevelCode,
    val settings: LevelSettings,
) {

    internal fun normalized(): LevelEntry = copy(settings = settings.normalized())
}
