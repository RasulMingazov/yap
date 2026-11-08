package app.yap.server.feature.models.usecase

import app.yap.server.core.llm.LlmConfig
import app.yap.server.core.llm.ProviderConnections
import app.yap.server.core.llm.StubLlmConfig
import app.yap.server.feature.models.model.StubTaskModel
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.persistence.StubTaskModelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class GetModelsUseCaseTest {

    @Test
    fun `GIVEN nothing saved WHEN reading THEN every task has the default`() = runTest {
        val env = Environment(models = emptyList())

        val overview = env.useCase()

        assertEquals(expected = StubTaskModel.stubTable(), actual = overview.tasks)
    }

    @Test
    fun `GIVEN a saved table WHEN reading THEN the tasks come as saved`() = runTest {
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())
        val env = Environment(models = table)

        val overview = env.useCase()

        assertEquals(expected = table, actual = overview.tasks)
    }

    @Test
    fun `GIVEN no DeepSeek key WHEN reading THEN DeepSeek is listed as not connected`() = runTest {
        val env = Environment(llmConfig = StubLlmConfig.stubLlmConfig(deepSeek = null))

        val overview = env.useCase()

        assertEquals(expected = listOf(false), actual = overview.providers.map { provider -> provider.connected })
    }

    private class Environment(
        llmConfig: LlmConfig = StubLlmConfig.stubLlmConfig(),
        models: List<TaskModel> = StubTaskModel.stubTable(),
    ) {

        val useCase = GetModelsUseCase(
            providerConnections = ProviderConnections(config = llmConfig),
            taskModelRepository = StubTaskModelRepository(models = models),
        )
    }
}
