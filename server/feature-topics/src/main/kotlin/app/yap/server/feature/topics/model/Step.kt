package app.yap.server.feature.topics.model

data class Step(
    val outcome: String,
    val passWhen: String,
    val task: String,
) {

    internal val isEmpty: Boolean
        get() = outcome.isEmpty() && passWhen.isEmpty() && task.isEmpty()
}
