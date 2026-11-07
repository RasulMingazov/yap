package app.yap.admin.feature.topics.presentation

import androidx.lifecycle.ViewModel
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.OpeningLine
import app.yap.admin.feature.topics.domain.Step
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent

internal class TopicViewModel : ViewModel() {

    data class DataState(
        val cast: List<Character> = emptyList(),
        val form: TopicContent = EMPTY_CONTENT,
        val isSaving: Boolean = false,
        val load: Load = Load.LOADING,
        val saved: Topic? = null,
        val saveStatus: SaveStatus? = null,
    )

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
            val choices: List<Choice>,
            val form: TopicContent,
            val guestDescription: String?,
            val isSaveEnabled: Boolean,
            val releaseDateText: String,
            val saveStatus: Status?,
            val subtitle: String,
            val title: String,
        ) : UiState {

            data class Choice(
                val interlocutor: Interlocutor,
                val isSelected: Boolean,
                val meta: String?,
                val name: String,
            )
        }

        data class Error(val message: String) : UiState

        data object Loading : UiState
    }

    private companion object {

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
            steps = listOf(
                Step(
                    outcome = "",
                    passWhen = "",
                    task = "",
                ),
            ),
            title = "",
        )
    }
}
