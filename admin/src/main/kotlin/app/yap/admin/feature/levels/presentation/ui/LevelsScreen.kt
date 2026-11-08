package app.yap.admin.feature.levels.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminSelect
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.CellTextField
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.core.design.TableCell
import app.yap.admin.core.design.TableHeaderCell
import app.yap.admin.core.design.ToolbarSaveAction
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Event
import app.yap.admin.feature.levels.presentation.LevelsViewModel.LevelForm
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
    ScreenBody(
        maxWidth = Dp.Unspecified,
        spacing = AdminTheme.Sizes.sectionGap,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
            LevelTable(
                content = content,
                onEvent = onEvent,
            )
            Note(text = TABLE_NOTE)
        }
        BlockSection(
            content = content,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun LevelTable(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    Column {
        Row {
            TableHeaderCell(
                text = "Уровень",
                modifier = Modifier
                    .width(AdminTheme.Sizes.levelCodeColumnWidth)
                    .padding(start = AdminTheme.Sizes.tableCellStartPadding),
            )
            TableHeaderCell(
                text = "Рейтинг",
                modifier = Modifier.width(AdminTheme.Sizes.levelColumnWidth),
            )
            TableHeaderCell(
                text = "Лексика",
                modifier = Modifier.width(AdminTheme.Sizes.levelColumnWidth),
            )
            TableHeaderCell(
                text = "Слов в фразе, макс.",
                modifier = Modifier.width(AdminTheme.Sizes.levelWordsColumnWidth),
            )
            TableHeaderCell(
                text = "Идиомы",
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = AdminTheme.Colors.line)
        content.rows.forEach { row ->
            key(row.code) {
                LevelRow(
                    content = content,
                    onEvent = onEvent,
                    row = row,
                )
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
    }
}

@Composable
private fun LevelRow(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
    row: UiState.Row,
) {
    val enabled = content.isSaveEnabled
    val form = row.form
    val onFormChange: (LevelForm) -> Unit = { changed -> onEvent(Event.FormChanged(row.code, changed)) }
    Row(
        modifier = Modifier
            .background(if (row.isChosen) AdminTheme.Colors.fill else Color.Transparent)
            .clickable(role = Role.Button) { onEvent(Event.LevelClicked(row.code)) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(
            modifier = Modifier
                .width(AdminTheme.Sizes.levelCodeColumnWidth)
                .padding(start = AdminTheme.Sizes.tableCellStartPadding),
        ) {
            Text(
                text = row.label,
                color = AdminTheme.Colors.ink,
                style = AdminTheme.Type.tableCell,
                fontWeight = FontWeight.SemiBold,
            )
        }
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.levelColumnWidth)) {
            Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
                row.range?.let { range ->
                    Text(
                        text = range,
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.tableCell,
                    )
                }
                if (row.isRatingFromShown) {
                    CellTextField(
                        enabled = enabled,
                        label = "Рейтинг от ${row.label}",
                        onValueChange = { ratingFrom -> onFormChange(form.copy(ratingFrom = ratingFrom)) },
                        value = form.ratingFrom,
                        modifier = Modifier.width(AdminTheme.Sizes.cellFieldWidth),
                    )
                }
            }
        }
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.levelColumnWidth)) {
            CellTextField(
                enabled = enabled,
                label = "Лексика ${row.label}",
                onValueChange = { vocabulary -> onFormChange(form.copy(vocabulary = vocabulary)) },
                value = form.vocabulary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.levelWordsColumnWidth)) {
            CellTextField(
                enabled = enabled,
                label = "Слов в фразе ${row.label}",
                onValueChange = { maxWords -> onFormChange(form.copy(maxWords = maxWords)) },
                value = form.maxWords,
                modifier = Modifier.width(AdminTheme.Sizes.cellFieldWidth),
            )
        }
        TableCell(modifier = Modifier.weight(1f)) {
            AdminSelect(
                enabled = enabled,
                label = { option -> option.label },
                onSelect = { option -> onFormChange(form.copy(idioms = option.idioms)) },
                options = content.idioms,
                placeholder = "Выберите",
                value = row.idioms,
            )
        }
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

private const val TABLE_NOTE =
    "Одна таблица на все темы и всех персонажей. Нажмите на уровень, чтобы открыть его блок промпта."

private val BLOCK_NOTE = buildAnnotatedString {
    append("Сервер подставляет этот блок вместо ")
    withStyle(AdminTheme.Type.mono.toSpanStyle().copy(background = AdminTheme.Colors.fill)) {
        append("{level.rules}")
    }
    append(" в промпты реплики и подсказок. Проверка шага и разбор от уровня не зависят.")
}
