package app.yap.admin.feature.server.domain

internal sealed interface ServerConnection {

    data class Connected(val environment: String) : ServerConnection

    data object Unauthorized : ServerConnection

    data object Unreachable : ServerConnection
}
