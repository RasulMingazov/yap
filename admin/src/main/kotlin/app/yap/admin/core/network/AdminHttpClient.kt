package app.yap.admin.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

internal fun adminHttpClient(
    engine: HttpClientEngine = CIO.create { requestTimeout = REQUEST_TIMEOUT_MILLIS },
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json()
    }
}

private const val REQUEST_TIMEOUT_MILLIS = 10_000L
