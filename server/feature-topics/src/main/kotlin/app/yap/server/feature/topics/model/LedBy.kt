package app.yap.server.feature.topics.model

sealed interface LedBy {

    data class Cast(val name: String) : LedBy

    data object Guest : LedBy
}
