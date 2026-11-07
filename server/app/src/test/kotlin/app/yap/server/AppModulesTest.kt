package app.yap.server

import app.yap.server.feature.characters.featureCharactersModule
import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.koin.dsl.module
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.verify

internal class AppModulesTest {

    @Test
    fun `GIVEN every module of the server WHEN verifying the graph THEN no binding is missing`() {
        val modules = appModules(config = StubAppConfig.stubAppConfig())

        module { includes(modules) }.verify()
    }

    @Test
    fun `GIVEN the characters module without the database module WHEN verifying it THEN the missing binding fails`() {
        val characters = featureCharactersModule()

        assertFailsWith<MissingKoinDefinitionException> { characters.verify() }
    }
}
