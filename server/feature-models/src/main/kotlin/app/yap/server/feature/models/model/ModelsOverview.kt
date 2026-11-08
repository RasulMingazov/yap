package app.yap.server.feature.models.model

data class ModelsOverview(
    val providers: List<ProviderState>,
    val tasks: List<TaskModel>,
)
