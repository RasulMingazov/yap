package app.yap.server.core.llm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ModelCatalogTest {

    @Test
    fun `GIVEN the catalog WHEN listing the providers THEN DeepSeek is the only one with its models in order`() {
        assertEquals(expected = listOf(ModelProviderId.DEEPSEEK), actual = ModelCatalog.providers)
        assertEquals(
            expected = listOf("deepseek-flash", "deepseek-v4-pro"),
            actual = ModelProviderId.DEEPSEEK.models,
        )
    }

    @Test
    fun `GIVEN the catalog WHEN reading the default THEN it is deepseek-flash of DeepSeek and in the catalog`() {
        val default = ModelCatalog.DEFAULT

        assertEquals(
            expected = ModelChoice(
                model = "deepseek-flash",
                provider = ModelProviderId.DEEPSEEK,
            ),
            actual = default,
        )
        assertTrue(ModelCatalog.contains(default))
    }

    @Test
    fun `GIVEN DeepSeek WHEN reading its key setting THEN it is DEEPSEEK_API_KEY`() {
        assertEquals(expected = "DEEPSEEK_API_KEY", actual = ModelProviderId.DEEPSEEK.keySetting)
    }

    @Test
    fun `GIVEN the catalog WHEN listing every model THEN no name is in two providers`() {
        val models = ModelCatalog.providers.flatMap { provider -> provider.models }

        assertEquals(expected = models.distinct(), actual = models)
    }

    @Test
    fun `GIVEN a model that is not a model of the provider WHEN asking the catalog THEN it is not in it`() {
        val choice = ModelChoice(
            model = "deepseek-chat",
            provider = ModelProviderId.DEEPSEEK,
        )

        assertFalse(ModelCatalog.contains(choice))
    }

    @Test
    fun `GIVEN a model of DeepSeek WHEN asking the catalog for its choice THEN the choice names DeepSeek`() {
        val choice = ModelCatalog.choiceOf("deepseek-v4-pro")

        assertEquals(
            expected = ModelChoice(
                model = "deepseek-v4-pro",
                provider = ModelProviderId.DEEPSEEK,
            ),
            actual = choice,
        )
    }

    @Test
    fun `GIVEN a model that no provider has WHEN asking the catalog for its choice THEN there is none`() {
        assertNull(ModelCatalog.choiceOf("deepseek-chat"))
    }
}
