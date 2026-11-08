package app.yap.admin.feature.models.domain

internal sealed interface SaveModelsResult {

    data object CatalogChanged : SaveModelsResult

    data object Failed : SaveModelsResult

    data object Invalid : SaveModelsResult

    data class Saved(val models: Models) : SaveModelsResult
}
