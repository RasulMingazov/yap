package app.yap.server.feature.levels.model

import app.yap.server.core.llm.ModelCatalog

internal object LevelModelAvailability {

    fun isAvailable(model: String): Boolean = ModelCatalog.choiceOf(model) != null

    fun areAvailable(entries: List<LevelEntry>): Boolean = entries
        .mapNotNull { entry -> entry.settings.model }
        .all { model -> isAvailable(model) }
}
