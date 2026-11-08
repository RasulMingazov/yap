package app.yap.server.feature.levels.model

import app.yap.server.core.llm.ModelCatalog

internal object LevelTableValidity {

    fun isValid(entries: List<LevelEntry>): Boolean = hasEveryCodeOnce(entries) &&
        entries.all { entry -> entry.settings.isFilled && ratingFitsCode(entry) } &&
        ratingsGrow(entries)

    fun isInCatalog(entries: List<LevelEntry>): Boolean = entries
        .mapNotNull { entry -> entry.settings.model }
        .all { model -> ModelCatalog.choiceOf(model) != null }

    private fun hasEveryCodeOnce(entries: List<LevelEntry>): Boolean = LevelCode.entries.all { code ->
        entries.count { entry -> entry.code == code } == 1
    }

    private fun ratingFitsCode(entry: LevelEntry): Boolean = entry.code.isRated == (entry.settings.ratingFrom != null)

    private fun ratingsGrow(entries: List<LevelEntry>): Boolean = entries
        .filter { entry -> entry.code.isRated }
        .sortedBy { entry -> entry.code }
        .mapNotNull { entry -> entry.settings.ratingFrom }
        .zipWithNext()
        .all { (lower, higher) -> lower < higher }
}
