package app.yap.server.admin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import java.security.MessageDigest

internal const val ADMIN_AUTHENTICATION = "admin"

internal fun Application.installAdminAuthentication(adminApiKey: String) {
    val expectedKey = adminApiKey.toByteArray()
    install(Authentication) {
        bearer(ADMIN_AUTHENTICATION) {
            authenticate { credential ->
                val isOwner = MessageDigest.isEqual(credential.token.toByteArray(), expectedKey)
                if (isOwner) UserIdPrincipal(OWNER) else null
            }
        }
    }
}

private const val OWNER = "owner"
