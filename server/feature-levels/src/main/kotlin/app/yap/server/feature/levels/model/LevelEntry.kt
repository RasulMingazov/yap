package app.yap.server.feature.levels.model

data class LevelEntry(
    val code: LevelCode,
    val settings: LevelSettings,
) {

    fun normalized(): LevelEntry = copy(settings = settings.normalized())
}
