package app.yap.admin.core.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal fun adminKtorfit(httpClient: HttpClient): Ktorfit = Ktorfit.Builder()
    .baseUrl(PLACEHOLDER_BASE_URL)
    .httpClient(httpClient)
    .build()

private const val PLACEHOLDER_BASE_URL = "http://localhost/"
