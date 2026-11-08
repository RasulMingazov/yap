package app.yap.server.feature.models.model

sealed interface SaveTaskModelsOutcome {

    data object CatalogChanged : SaveTaskModelsOutcome

    data object Invalid : SaveTaskModelsOutcome

    data class Saved(val overview: ModelsOverview) : SaveTaskModelsOutcome
}
