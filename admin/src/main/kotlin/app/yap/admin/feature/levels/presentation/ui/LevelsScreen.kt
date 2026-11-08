package app.yap.admin.feature.levels.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.core.design.ToolbarSaveAction
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Event
import app.yap.admin.feature.levels.presentation.LevelsViewModel.News
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState

@Composable
internal fun LevelsScreen(viewModel: LevelsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.onEvent(Event.Opened)
        viewModel.news.collect { news ->
            when (news) {
                is News.ShowSnackbar -> snackbarHostState.showSnackbar(news.message)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LevelsContent(
            onEvent = viewModel::onEvent,
            uiState = uiState,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun LevelsContent(
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        when (uiState) {
            is UiState.Content -> {
                LevelsToolbar {
                    ToolbarSaveAction(status = uiState.saveStatus) {
                        PrimaryButton(
                            enabled = uiState.isSaveEnabled,
                            onClick = { onEvent(Event.SaveClicked) },
                            text = "Сохранить",
                        )
                    }
                }
                LevelsForm(
                    content = uiState,
                    onEvent = onEvent,
                )
            }
            is UiState.Error -> {
                LevelsToolbar()
                ScreenBody {
                    LoadError(
                        message = uiState.message,
                        onRetryClick = { onEvent(Event.RetryClicked) },
                    )
                }
            }
            is UiState.Loading -> {
                LevelsToolbar()
                ScreenBody {
                    Loader()
                }
            }
        }
    }
}

@Composable
private fun LevelsToolbar(actions: @Composable RowScope.() -> Unit = {}) {
    ScreenToolbar(
        actions = actions,
        subtitle = "Как собеседник говорит с каждым уровнем рейтинга",
        title = "Уровни",
    )
}

@Composable
private fun LevelsForm(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    ScreenBody(maxWidth = Dp.Unspecified) {
        BlockSection(
            content = content,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun BlockSection(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    val block = content.block
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(title = block.title)
            block.rating?.let { rating ->
                Text(
                    text = rating,
                    color = AdminTheme.Colors.muted,
                    style = AdminTheme.Type.sub,
                )
            }
        }
        AdminTextField(
            label = "Текст блока",
            onValueChange = { text -> onEvent(Event.FormChanged(block.code, block.form.copy(block = text))) },
            value = block.form.block,
            enabled = content.isSaveEnabled,
            minLines = BLOCK_LINES,
            modifier = Modifier.fillMaxWidth(),
            textStyle = AdminTheme.Type.mono,
        )
        Note(text = BLOCK_NOTE)
    }
}

private const val BLOCK_LINES = 7

private val BLOCK_NOTE = buildAnnotatedString {
    append("Сервер подставляет этот блок вместо ")
    withStyle(AdminTheme.Type.mono.toSpanStyle().copy(background = AdminTheme.Colors.fill)) {
        append("{level.rules}")
    }
    append(" в промпты реплики и подсказок. Проверка шага и разбор от уровня не зависят.")
}
