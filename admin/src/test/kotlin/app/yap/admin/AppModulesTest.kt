package app.yap.admin

import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicViewModel
import io.ktor.client.engine.HttpClientEngine
import java.time.LocalDate
import kotlin.test.Test
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

internal class AppModulesTest {

    @Test
    fun `GIVEN every module of the admin app WHEN verifying the graph THEN no binding is missing`() {
        val modules = appModules()

        module { includes(modules) }.verify(
            extraTypes = listOf(HttpClientEngine::class),
            injections = injectedParameters(
                definition<CharacterViewModel>(CharacterId::class),
                definition<TopicViewModel>(LocalDate::class, TopicId::class),
            ),
        )
    }
}
