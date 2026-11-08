package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.usecase.StubGetCharactersUseCase
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.usecase.StubGetTopicUseCase
import app.yap.admin.feature.topics.domain.usecase.StubSaveTopicUseCase
import app.yap.admin.feature.topics.presentation.TopicViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicViewModel.News
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class TopicViewModelTest {

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
    fun `GIVEN a saved topic WHEN the screen opens THEN the fields hold the topic`() = runTest(dispatcher) {
        val env = Environment(topicId = TopicId(StubTopic.TOPIC_ID))

        advanceUntilIdle()

        assertEquals(expected = StubTopic.stubContent(), actual = env.content().form)
    }

    @Test
    fun `GIVEN the topic does not load WHEN the screen opens THEN it shows the load error`() = runTest(dispatcher) {
        val env = Environment(topicId = TopicId(StubTopic.TOPIC_ID))
        env.getTopicUseCase.invokeCall.returns(LoadTopicResult.Failed)

        advanceUntilIdle()

        assertIs<UiState.Error>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the cast does not load WHEN Retry is clicked THEN the screen shows the form`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        env.getCharactersUseCase.invokeCall.returns(LoadResult.Failed)
        advanceUntilIdle()
        env.getCharactersUseCase.invokeCall.returns(LoadResult.Loaded(emptyList()))

        env.viewModel.onEvent(Event.RetryClicked)
        advanceUntilIdle()

        assertIs<UiState.Content>(env.viewModel.uiState.value)
    }

    @Test
    fun `GIVEN Saved WHEN a field changes THEN the form takes it and the status clears`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.FormChanged(StubTopic.stubContent(title = "At the bank")))
        advanceUntilIdle()

        assertEquals(expected = "At the bank" to null, actual = env.content().form.title to env.content().saveStatus)
    }

    @Test
    fun `GIVEN a new topic WHEN a day is picked THEN the popup shows it`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.ReleaseDatePicked(LocalDate.of(2025, 10, 25)))
        advanceUntilIdle()

        assertEquals(expected = "Суббота, 25 октября", actual = env.content().releaseDateText)
    }

    @Test
    fun `GIVEN Guest is chosen WHEN its description is typed THEN the guest has it`() = runTest(dispatcher) {
        val env = Environment(topicId = TopicId(StubTopic.TOPIC_ID))
        advanceUntilIdle()

        env.viewModel.onEvent(Event.InterlocutorChosen(Interlocutor.Guest(description = "")))
        env.viewModel.onEvent(Event.GuestDescriptionChanged("A tourist"))
        advanceUntilIdle()

        assertEquals(expected = "A tourist", actual = env.content().guestDescription)
    }

    @Test
    fun `GIVEN a new topic WHEN the save succeeds THEN the next save updates the saved topic`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        advanceUntilIdle()
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveTopicUseCase.invokeCall.calledWith(TopicId(StubTopic.TOPIC_ID), StubTopic.stubContent())
    }

    @Test
    fun `GIVEN a new topic WHEN the save succeeds THEN the title and Saved show`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        val saved = Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
        assertEquals(expected = StubTopic.TITLE to saved, actual = env.content().title to env.content().saveStatus)
    }

    @Test
    fun `GIVEN the server answers 400 WHEN saving THEN the form stays and the error shows`() = runTest(dispatcher) {
        val env = Environment(topicId = null, saveResult = SaveTopicResult.Invalid)
        advanceUntilIdle()
        env.viewModel.onEvent(Event.FormChanged(StubTopic.stubContent(title = "")))

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        val content = env.content()
        assertEquals(expected = "" to Status.Tone.ERROR, actual = content.form.title to content.saveStatus?.tone)
    }

    @Test
    fun `GIVEN the server does not answer WHEN saving THEN a snackbar says the save failed`() = runTest(dispatcher) {
        val env = Environment(topicId = null, saveResult = SaveTopicResult.Failed)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        assertEquals(
            expected = News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."),
            actual = env.viewModel.news.first(),
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN Save draft is clicked THEN the topic is saved once`() = runTest(dispatcher) {
        val env = Environment(topicId = null)
        advanceUntilIdle()

        env.viewModel.onEvent(Event.SaveClicked)
        env.viewModel.onEvent(Event.SaveClicked)
        advanceUntilIdle()

        env.saveTopicUseCase.invokeCall.called(times = 1)
    }

    private class Environment(
        topicId: TopicId?,
        saveResult: SaveTopicResult = SaveTopicResult.Saved(StubTopic.stubTopic()),
    ) {

        val getCharactersUseCase = StubGetCharactersUseCase()
        val getTopicUseCase = StubGetTopicUseCase()
        val saveTopicUseCase = StubSaveTopicUseCase(result = saveResult)
        val viewModel = TopicViewModel(
            getCharactersUseCase = getCharactersUseCase,
            getTopicUseCase = getTopicUseCase,
            saveTopicUseCase = saveTopicUseCase,
            topicId = topicId,
        )

        fun content(): UiState.Content = viewModel.uiState.value as UiState.Content
    }
}
