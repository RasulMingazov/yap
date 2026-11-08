package app.yap.admin.feature.models.domain

internal data class Models(
    val providers: List<Provider>,
    val tasks: List<TaskModel>,
) {

    fun hasEveryTask(): Boolean = tasks.map { taskModel -> taskModel.task } == ModelTask.entries
}
