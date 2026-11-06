package app.yap.server

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ApplicationTest {

    @Test
    fun `GIVEN no admin key WHEN listing the characters THEN the call is unauthorized`() = testApplication {
        application {
            serverModule(
                characterRepository = StubCharacterRepository(),
                config = StubAppConfig.stubAppConfig(),
            )
        }

        val response = client.get("/admin/characters")

        assertEquals(expected = HttpStatusCode.Unauthorized, actual = response.status)
    }
}
