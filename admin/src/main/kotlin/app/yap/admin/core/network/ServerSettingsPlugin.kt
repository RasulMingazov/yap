package app.yap.admin.core.network

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.ServerSettingsRepository
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.encodedPath

internal fun serverSettingsPlugin(serverSettingsRepository: ServerSettingsRepository): ClientPlugin<Unit> =
    createClientPlugin("ServerSettings") {
        onRequest { request, _ ->
            if (!request.headers.contains(HttpHeaders.Authorization)) {
                serverSettingsRepository
                    .get()
                    ?.let { settings -> request.sendTo(settings) }
            }
        }
    }

private fun HttpRequestBuilder.sendTo(settings: ServerSettings) {
    val server = Url(settings.address)
    url.protocol = server.protocol
    url.host = server.host
    url.port = server.specifiedPort
    url.encodedPath = "${server.encodedPath.trimEnd('/')}/${url.encodedPath.trimStart('/')}"
    bearerAuth(settings.key)
}
