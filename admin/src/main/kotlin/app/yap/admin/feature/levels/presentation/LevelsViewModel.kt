package app.yap.admin.feature.levels.presentation

import androidx.lifecycle.ViewModel
import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelSettings

internal class LevelsViewModel : ViewModel() {

    data class DataState(
        val chosen: LevelCode = LevelCode.B1,
        val forms: Map<LevelCode, LevelForm> = emptyMap(),
        val isSaving: Boolean = false,
        val levels: List<Level> = emptyList(),
        val load: Load = Load.LOADING,
        val saveStatus: SaveStatus? = null,
    ) {

        fun loaded(levels: List<Level>): DataState = copy(
            forms = levels.associate { level -> level.code to (level.settings?.let(LevelForm::of) ?: LevelForm.EMPTY) },
            isSaving = false,
            levels = levels,
            load = Load.LOADED,
        )
    }

    data class LevelForm(
        val block: String,
        val idioms: Idioms,
        val maxWords: String,
        val ratingFrom: String,
        val vocabulary: String,
    ) {

        companion object {

            val EMPTY = LevelForm(
                block = "",
                idioms = Idioms.NEVER,
                maxWords = "",
                ratingFrom = "",
                vocabulary = "",
            )

            fun of(settings: LevelSettings): LevelForm = LevelForm(
                block = settings.block,
                idioms = settings.idioms,
                maxWords = settings.maxWords.toString(),
                ratingFrom = settings.ratingFrom?.toString().orEmpty(),
                vocabulary = settings.vocabulary,
            )
        }
    }

    enum class Load {
        FAILED,
        LOADED,
        LOADING,
    }

    sealed interface SaveStatus {

        data object Invalid : SaveStatus

        data object Saved : SaveStatus

        data class UnreadNumber(val code: LevelCode) : SaveStatus
    }

    sealed interface UiState {

        data class Content(
            val block: Block,
            val idioms: List<IdiomsOption>,
            val isSaveEnabled: Boolean,
            val rows: List<Row>,
            val saveStatus: Status?,
        ) : UiState

        data class Error(val message: String) : UiState

        data object Loading : UiState

        data class Block(
            val rating: String?,
            val text: String,
            val title: String,
        )

        data class IdiomsOption(
            val idioms: Idioms,
            val label: String,
        )

        data class Row(
            val code: LevelCode,
            val form: LevelForm,
            val isChosen: Boolean,
            val label: String,
            val range: String?,
        )
    }
}
