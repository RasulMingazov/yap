package app.yap.server.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AppEnvironmentTest {

    @Test
    fun `GIVEN staging WHEN APP_ENV is read THEN the environment is deployed`() {
        val environment = AppEnvironment.from("Staging")

        assertEquals(expected = AppEnvironment.STAGING to true, actual = environment to environment.isDeployed)
    }

    @Test
    fun `GIVEN development WHEN APP_ENV is read THEN the environment is not deployed`() {
        assertEquals(expected = false, actual = AppEnvironment.from("development").isDeployed)
    }

    @Test
    fun `GIVEN an unknown value WHEN APP_ENV is read THEN startup fails`() {
        assertFailsWith<IllegalArgumentException> { AppEnvironment.from("prod") }
    }
}
