package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.usecase.StubGetTopicsUseCase
import app.yap.admin.feature.topics.presentation.TopicsViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class TopicsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the server has topics WHEN the screen opens THEN it shows them`() = runTest(dispatcher) {
        val env = Environment(topics = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())))

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a shown list WHEN the screen opens again THEN the list stays until the new list replaces it`() =
        runTest(dispatcher) {
            val env = Environment(topics = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())))
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            val rowCounts = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                env.viewModel.uiState.collect { state -> rowCounts += (state as? UiState.Content)?.rows?.size }
            }
            val twoTopics = listOf(StubTopic.stubTopic(), StubTopic.stubTopic(topicId = "second-topic"))
            env.getTopicsUseCase.invokeCall.returns(LoadTopicsResult.Loaded(twoTopics))

            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            assertEquals(expected = listOf<Int?>(1, 2), actual = rowCounts)
        }

    @Test
    fun `GIVEN a shown list WHEN the list does not update THEN the list stays`() = runTest(dispatcher) {
        val env = Environment(topics = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())))
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getTopicsUseCase.invokeCall.returns(LoadTopicsResult.Failed)

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a load in progress WHEN the screen opens again THEN the topics load once`() = runTest(dispatcher) {
        val env = Environment(topics = LoadTopicsResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getTopicsUseCase.invokeCall.callCount)
    }

    @Test
    fun `GIVEN the topics did not load WHEN Retry is clicked THEN they load again`() = runTest(dispatcher) {
        val env = Environment(topics = LoadTopicsResult.Failed)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getTopicsUseCase.invokeCall.returns(LoadTopicsResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertIs<UiState.Empty>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the topics are loading WHEN Retry is clicked THEN they load once`() = runTest(dispatcher) {
        val env = Environment(topics = LoadTopicsResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getTopicsUseCase.invokeCall.callCount)
    }

    private class Environment(
        topics: LoadTopicsResult,
    ) {

        val getTopicsUseCase = StubGetTopicsUseCase(topics = topics)
        val viewModel = TopicsViewModel(getTopicsUseCase = getTopicsUseCase)
    }
}
