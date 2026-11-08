package app.yap.admin.feature.topics.domain

internal sealed interface LedBy {

    data class Cast(val name: String) : LedBy

    data object Guest : LedBy
}
