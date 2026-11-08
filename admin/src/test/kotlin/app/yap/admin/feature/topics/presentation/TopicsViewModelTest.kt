package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.usecase.StubGetTopicCalendarUseCase
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
    fun `GIVEN the server has a calendar WHEN the screen opens THEN it shows its days`() = runTest(dispatcher) {
        val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a shown calendar WHEN the screen opens again THEN the calendar stays until the new one replaces it`() =
        runTest(dispatcher) {
            val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            val rowCounts = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                env.viewModel.uiState.collect { state -> rowCounts += (state as? UiState.Content)?.rows?.size }
            }
            env.getTopicCalendarUseCase.invokeCall.returns(LoadResult.Loaded(TWO_DAYS))

            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()

            assertEquals(expected = listOf<Int?>(1, 2), actual = rowCounts)
        }

    @Test
    fun `GIVEN a shown calendar WHEN the reload fails THEN the calendar stays`() = runTest(dispatcher) {
        val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        env.getTopicCalendarUseCase.invokeCall.returns(LoadResult.Failed)

        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = assertIs<UiState.Content>(env.viewModel.uiState.value).rows.size)
    }

    @Test
    fun `GIVEN a load in progress WHEN the screen opens again THEN the calendar loads once`() = runTest(dispatcher) {
        val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getTopicCalendarUseCase.invokeCall.callCount)
    }

    @Test
    fun `GIVEN the calendar did not load WHEN Retry is clicked THEN it loads again`() = runTest(dispatcher) {
        val env = Environment(calendar = LoadResult.Failed)
        env.viewModel.onEvent(Event.Opened)
        advanceUntilIdle()
        val states = mutableListOf<UiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            env.viewModel.uiState.collect { state -> states += state }
        }
        env.getTopicCalendarUseCase.invokeCall.returns(LoadResult.Loaded(ONE_DAY))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(UiState.Error::class, UiState.Loading::class, UiState.Content::class),
            actual = states.map { state -> state::class },
        )
    }

    @Test
    fun `GIVEN a shown calendar and a failed reload WHEN Retry is clicked THEN the new calendar replaces it`() =
        runTest(dispatcher) {
            val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.getTopicCalendarUseCase.invokeCall.returns(LoadResult.Failed)
            env.viewModel.onEvent(Event.Opened)
            advanceUntilIdle()
            env.getTopicCalendarUseCase.invokeCall.returns(LoadResult.Loaded(TWO_DAYS))

            env.viewModel.onEvent(Event.RetryClicked)
            advanceUntilIdle()

            assertEquals(expected = 2, actual = assertIs<UiState.Content>(env.viewModel.uiState.value).rows.size)
        }

    @Test
    fun `GIVEN the calendar is loading WHEN Retry is clicked THEN it loads once`() = runTest(dispatcher) {
        val env = Environment(calendar = LoadResult.Loaded(ONE_DAY))

        env.viewModel.onEvent(Event.Opened)
        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertEquals(expected = 1, actual = env.getTopicCalendarUseCase.invokeCall.callCount)
    }

    private class Environment(
        calendar: LoadResult<TopicCalendar>,
    ) {

        val getTopicCalendarUseCase = StubGetTopicCalendarUseCase(calendar = calendar)
        val viewModel = TopicsViewModel(getTopicCalendarUseCase = getTopicCalendarUseCase)
    }

    private companion object {

        val ONE_DAY = StubTopicCalendar.stubTopicCalendar(rows = listOf(StubTopicCalendar.stubCalendarRow()))
        val TWO_DAYS = StubTopicCalendar.stubTopicCalendar(
            rows = listOf(
                StubTopicCalendar.stubCalendarRow(),
                StubTopicCalendar.stubCalendarRow(
                    date = StubTopic.RELEASE_DATE.plusDays(1),
                    topic = StubTopicCalendar.stubCalendarTopic(topicId = "second-topic"),
                ),
            ),
        )
    }
}
