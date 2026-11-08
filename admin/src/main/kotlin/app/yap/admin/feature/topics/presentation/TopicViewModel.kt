package app.yap.admin.feature.topics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.mapState
import app.yap.admin.feature.characters.domain.CharacterSummary
import app.yap.admin.feature.characters.domain.usecase.GetCharactersUseCase
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.OpeningLine
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.Step
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.usecase.GetTopicUseCase
import app.yap.admin.feature.topics.domain.usecase.SaveTopicUseCase
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TopicViewModel(
    releaseDate: LocalDate?,
    topicId: TopicId?,
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val saveTopicUseCase: SaveTopicUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(
        DataState(
            form = EMPTY_CONTENT.copy(releaseDate = releaseDate),
            topicId = topicId,
        ),
    )
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, TopicUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: Event) = when (event) {
        is Event.AddStepClicked -> edit { form -> form.copy(steps = form.steps + EMPTY_STEP) }
        is Event.DurationChanged -> onDurationChanged(event.text)
        is Event.FormChanged -> edit { event.form }
        is Event.GuestDescriptionChanged -> edit { form ->
            form.copy(interlocutor = Interlocutor.Guest(description = event.description))
        }
        is Event.InterlocutorChosen -> onInterlocutorChosen(event.interlocutor)
        is Event.ReleaseDatePicked -> onReleaseDatePicked(event.date)
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
        is Event.StepChanged -> onStepChanged(event.index, event.step)
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    private fun onDurationChanged(text: String) {
        if (text.length > MAX_DURATION_DIGITS || !text.all(Char::isDigit)) return
        edit { form -> form.copy(durationMinutes = text.toIntOrNull()) }
    }

    private fun edit(transform: (TopicContent) -> TopicContent) = dataState.update { state ->
        state
            .withForm(transform(state.form))
            .copy(saveStatus = state.saveStatus.takeIf { status -> status == SaveStatus.REJECTED })
    }

    private fun onInterlocutorChosen(interlocutor: Interlocutor) {
        if (interlocutor == dataState.value.form.interlocutor) return
        edit { form -> form.copy(interlocutor = interlocutor) }
    }

    private fun onReleaseDatePicked(date: LocalDate) {
        if (date == dataState.value.form.releaseDate) return
        edit { form -> form.copy(releaseDate = date) }
    }

    private fun onRetryClicked() {
        if (dataState.value.load != Load.FAILED) return
        load()
    }

    private fun load() {
        dataState.update { state -> state.copy(load = Load.LOADING) }
        viewModelScope.launch {
            when (val cast = getCharactersUseCase()) {
                is LoadResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
                is LoadResult.Loaded -> onCastLoaded(cast.value)
            }
        }
    }

    private suspend fun onCastLoaded(cast: List<CharacterSummary>) {
        val topicId = dataState.value.topicId
        if (topicId == null) {
            dataState.update { state ->
                state.copy(
                    cast = cast,
                    load = Load.LOADED,
                )
            }
            return
        }
        when (val result = getTopicUseCase(topicId)) {
            is LoadTopicResult.Failed -> dataState.update { state -> state.copy(load = Load.FAILED) }
            is LoadTopicResult.Loaded -> dataState.update { state ->
                state
                    .copy(cast = cast)
                    .saved(result.topic)
            }
        }
    }

    private fun onSaveClicked() {
        val state = dataState.value
        if (state.isSaving) return
        dataState.update { current ->
            current.copy(
                isSaving = true,
                saveStatus = null,
            )
        }
        viewModelScope.launch {
            when (val result = saveTopicUseCase(state.saved?.topicId, state.form)) {
                is SaveTopicResult.Failed -> onSaveFailed()
                is SaveTopicResult.Invalid -> dataState.update { current ->
                    current.copy(
                        isSaving = false,
                        saveStatus = SaveStatus.REJECTED,
                    )
                }
                is SaveTopicResult.Saved -> dataState.update { current ->
                    current
                        .saved(result.topic)
                        .copy(saveStatus = SaveStatus.SAVED)
                }
            }
        }
    }

    private suspend fun onSaveFailed() {
        dataState.update { state -> state.copy(isSaving = false) }
        newsChannel.send(News.ShowSnackbar("Не удалось сохранить. Попробуйте ещё раз."))
    }

    private fun onStepChanged(changedIndex: Int, changedStep: Step) = edit { form ->
        form.copy(steps = form.steps.mapIndexed { index, step -> if (index == changedIndex) changedStep else step })
    }

    data class DataState(
        val cast: List<CharacterSummary> = emptyList(),
        val form: TopicContent = EMPTY_CONTENT,
        val guestDescription: String = "",
        val isSaving: Boolean = false,
        val load: Load = Load.LOADING,
        val saved: Topic? = null,
        val saveStatus: SaveStatus? = null,
        val topicId: TopicId? = null,
    ) {

        fun saved(topic: Topic): DataState = withForm(topic.content)
            .copy(
                isSaving = false,
                load = Load.LOADED,
                saved = topic,
            )

        fun withForm(form: TopicContent): DataState = copy(
            form = form,
            guestDescription = (form.interlocutor as? Interlocutor.Guest)?.description ?: guestDescription,
        )
    }

    enum class Load {
        FAILED,
        LOADED,
        LOADING,
    }

    enum class SaveStatus {
        REJECTED,
        SAVED,
    }

    sealed interface UiState {

        data class Content(
            val castNote: String?,
            val choices: List<Choice>,
            val form: TopicContent,
            val guestDescription: String?,
            val hint: String?,
            val isSaveEnabled: Boolean,
            val releaseDateText: String?,
            val saveStatus: Status?,
            val subtitle: String,
            val title: String,
        ) : UiState {

            data class Choice(
                val interlocutor: Interlocutor,
                val isSelected: Boolean,
                val meta: String,
                val name: String,
            )
        }

        data class Error(
            val message: String,
            val toolbar: Toolbar,
        ) : UiState

        data class Loading(val toolbar: Toolbar) : UiState

        data class Toolbar(
            val subtitle: String?,
            val title: String,
        )
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data object AddStepClicked : Event

        data class DurationChanged(val text: String) : Event

        data class FormChanged(val form: TopicContent) : Event

        data class GuestDescriptionChanged(val description: String) : Event

        data class InterlocutorChosen(val interlocutor: Interlocutor) : Event

        data class ReleaseDatePicked(val date: LocalDate) : Event

        data object RetryClicked : Event

        data object SaveClicked : Event

        data class StepChanged(
            val index: Int,
            val step: Step,
        ) : Event
    }

    private companion object {

        const val MAX_DURATION_DIGITS = 3

        val EMPTY_STEP = Step(
            outcome = "",
            passWhen = "",
            task = "",
        )

        val EMPTY_CONTENT = TopicContent(
            description = "",
            durationMinutes = null,
            interlocutor = null,
            openingLine = OpeningLine(
                english = "",
                translation = "",
            ),
            releaseDate = null,
            role = "",
            setting = "",
            steps = listOf(EMPTY_STEP),
            title = "",
        )
    }
}
