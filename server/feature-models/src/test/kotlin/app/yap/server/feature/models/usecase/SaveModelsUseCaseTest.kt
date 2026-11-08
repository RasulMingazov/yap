package app.yap.server.feature.models.usecase

import app.yap.server.core.llm.LlmConfig
import app.yap.server.core.llm.ProviderConnections
import app.yap.server.core.llm.StubLlmConfig
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.SaveTaskModelsOutcome
import app.yap.server.feature.models.model.StubTaskModel
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.persistence.StubTaskModelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

internal class SaveModelsUseCaseTest {

    @Test
    fun `GIVEN every task once in the catalog WHEN saving THEN the table is stored and the overview comes back`() = runTest {
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())
        val env = Environment(models = table)

        val outcome = env.useCase(table)

        env.taskModelRepository.saveCall.calledWith(table)
        assertEquals(expected = table, actual = assertIs<SaveTaskModelsOutcome.Saved>(outcome).overview.tasks)
    }

    @Test
    fun `GIVEN a table without Hints WHEN saving THEN nothing is stored`() = runTest {
        val env = Environment()
        val table = StubTaskModel.stubTable().filter { model -> model.task != ModelTask.TOPIC_HINTS }

        val outcome = env.useCase(table)

        env.taskModelRepository.saveCall.notCalled()
        assertEquals(expected = SaveTaskModelsOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN a model outside the catalog WHEN saving THEN nothing is stored`() = runTest {
        val env = Environment()
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice(model = "deepseek-chat"))

        val outcome = env.useCase(table)

        env.taskModelRepository.saveCall.notCalled()
        assertEquals(expected = SaveTaskModelsOutcome.CatalogChanged, actual = outcome)
    }

    @Test
    fun `GIVEN no DeepSeek key WHEN saving every task with DeepSeek THEN the table is stored`() = runTest {
        val env = Environment(llmConfig = StubLlmConfig.stubLlmConfig(deepSeek = null))
        val table = StubTaskModel.stubTable()

        env.useCase(table)

        env.taskModelRepository.saveCall.calledWith(table)
    }

    private class Environment(
        llmConfig: LlmConfig = StubLlmConfig.stubLlmConfig(),
        models: List<TaskModel> = StubTaskModel.stubTable(),
    ) {

        val taskModelRepository = StubTaskModelRepository(models = models)
        val useCase = SaveModelsUseCase(
            getModelsUseCase = GetModelsUseCase(
                providerConnections = ProviderConnections(config = llmConfig),
                taskModelRepository = taskModelRepository,
            ),
            taskModelRepository = taskModelRepository,
        )
    }
}
