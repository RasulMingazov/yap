package app.yap.admin.feature.topics.domain

internal data class Step(
    val outcome: String,
    val passWhen: String,
    val task: String,
)
