package app.yap.server

import kotlin.test.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

internal class AppModulesTest {

    @Test
    fun `GIVEN every module of the server WHEN verifying the graph THEN no binding is missing`() {
        val modules = appModules(config = StubAppConfig.stubAppConfig())

        module { includes(modules) }.verify()
    }
}
