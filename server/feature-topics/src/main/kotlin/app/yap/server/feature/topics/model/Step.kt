package app.yap.server.feature.topics.model

data class Step(
    val outcome: String,
    val passWhen: String,
    val task: String,
) {

    internal val isComplete: Boolean
        get() = outcome.isNotEmpty() && passWhen.isNotEmpty() && task.isNotEmpty()

    internal val isEmpty: Boolean
        get() = outcome.isEmpty() && passWhen.isEmpty() && task.isEmpty()

    internal fun normalized(): Step = copy(
        outcome = outcome.trim(),
        passWhen = passWhen.trim(),
        task = task.trim(),
    )
}
