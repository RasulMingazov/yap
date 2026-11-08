package app.yap.admin.feature.models.domain

internal data class Provider(
    val code: String,
    val connected: Boolean,
    val keySetting: String,
    val models: List<String>,
    val name: String,
)
