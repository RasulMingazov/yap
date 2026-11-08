package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.EmptyMessage
import app.yap.admin.core.design.LinkButton
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.SecondaryButton
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.core.design.StatusText
import app.yap.admin.core.design.TableCell
import app.yap.admin.core.design.TableHeaderCell
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import app.yap.admin.feature.topics.presentation.TopicsViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState

@Composable
internal fun TopicsScreen(
    onNewTopicClick: () -> Unit,
    onTopicClick: (TopicId) -> Unit,
    viewModel: TopicsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.onEvent(Event.Opened)
    }
    TopicsContent(
        onEvent = viewModel::onEvent,
        onNewTopicClick = onNewTopicClick,
        onTopicClick = onTopicClick,
        uiState = uiState,
    )
}

@Composable
internal fun TopicsContent(
    onEvent: (Event) -> Unit,
    onNewTopicClick: () -> Unit,
    onTopicClick: (TopicId) -> Unit,
    uiState: UiState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenToolbar(
            actions = {
                PrimaryButton(
                    enabled = true,
                    onClick = onNewTopicClick,
                    text = "Новая тема",
                )
            },
            subtitle = "Одна тема в день",
            title = "Темы",
        )
        ScreenBody(
            maxWidth = Dp.Unspecified,
            spacing = AdminTheme.Sizes.sectionGap,
        ) {
            when (uiState) {
                is UiState.Content -> {
                    uiState.reloadError?.let { reloadError ->
                        ReloadBanner(
                            onRetryClick = { onEvent(Event.RetryClicked) },
                            reloadError = reloadError,
                        )
                    }
                    TodoSection(
                        onCreateClick = onNewTopicClick,
                        onTopicClick = onTopicClick,
                        todo = uiState.todo,
                    )
                    AllTopicsSection(
                        onCreateClick = onNewTopicClick,
                        onTopicClick = onTopicClick,
                        rows = uiState.rows,
                    )
                }
                is UiState.Error -> LoadError(
                    message = uiState.message,
                    onRetryClick = { onEvent(Event.RetryClicked) },
                )
                is UiState.Loading -> Loader()
            }
        }
    }
}

@Composable
private fun ReloadBanner(
    onRetryClick: () -> Unit,
    reloadError: UiState.ReloadError,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AdminTheme.Colors.fill,
                shape = RoundedCornerShape(AdminTheme.Sizes.cornerRadius),
            )
            .padding(
                horizontal = AdminTheme.Sizes.hintHorizontalPadding,
                vertical = AdminTheme.Sizes.hintVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = reloadError.message,
            modifier = Modifier.weight(1f),
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.body,
        )
        if (reloadError.isRetrying) {
            Loader()
        }
        SecondaryButton(
            enabled = !reloadError.isRetrying,
            onClick = onRetryClick,
            text = "Повторить",
        )
    }
}

@Composable
private fun TodoSection(
    onCreateClick: () -> Unit,
    onTopicClick: (TopicId) -> Unit,
    todo: UiState.TodoList,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
        SectionHeader(title = "Нужно сделать")
        when (todo) {
            is UiState.TodoList.Empty -> EmptyMessage(text = todo.message)
            is UiState.TodoList.Entries -> Column {
                HorizontalDivider(color = AdminTheme.Colors.line)
                todo.entries.forEach { entry ->
                    when (entry) {
                        is UiState.TodoEntry.Draft -> TodoRow(
                            action = "Открыть",
                            date = entry.date,
                            dateColor = AdminTheme.Colors.ink,
                            note = entry.note,
                            onClick = { onTopicClick(entry.topicId) },
                            title = entry.title,
                        )
                        is UiState.TodoEntry.EmptyDay -> TodoRow(
                            action = "Создать",
                            date = entry.date,
                            dateColor = AdminTheme.Colors.bad,
                            note = entry.note,
                            onClick = onCreateClick,
                            title = entry.title,
                        )
                    }
                    HorizontalDivider(color = AdminTheme.Colors.line)
                }
            }
        }
    }
}

@Composable
private fun TodoRow(
    action: String,
    date: String,
    dateColor: Color,
    note: String,
    onClick: () -> Unit,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.rowMinHeight)
            .padding(vertical = AdminTheme.Sizes.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = date,
            modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth),
            color = dateColor,
            style = AdminTheme.Type.body,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(title)
                }
                append(" ")
                val noteStyle = AdminTheme.Type.sub
                    .toSpanStyle()
                    .copy(color = AdminTheme.Colors.muted)
                withStyle(noteStyle) {
                    append(note)
                }
            },
            modifier = Modifier.weight(1f),
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.body,
        )
        SecondaryButton(
            enabled = true,
            onClick = onClick,
            text = action,
        )
    }
}

@Composable
private fun AllTopicsSection(
    onCreateClick: () -> Unit,
    onTopicClick: (TopicId) -> Unit,
    rows: List<UiState.Row>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
        SectionHeader(title = "Все темы")
        Column {
            TopicTableHeader()
            HorizontalDivider(color = AdminTheme.Colors.line)
            rows.forEach { row ->
                when (row) {
                    is UiState.Row.EmptyDay -> EmptyDayRow(
                        onCreateClick = onCreateClick,
                        row = row,
                    )
                    is UiState.Row.Topic -> TopicRow(
                        onTopicClick = onTopicClick,
                        row = row,
                    )
                }
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
        Note(text = "Каждый день пользователи получают одну тему. Опубликованная тема выходит в свою дату.")
    }
}

@Composable
private fun TopicTableHeader() {
    Row {
        TableHeaderCell(
            text = "Дата",
            modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth),
        )
        TableHeaderCell(
            text = "Тема",
            modifier = Modifier.weight(1f),
        )
        TableHeaderCell(
            text = "Ведёт",
            modifier = Modifier.width(AdminTheme.Sizes.ledByColumnWidth),
        )
        TableHeaderCell(
            text = "Шаги",
            modifier = Modifier.width(AdminTheme.Sizes.stepsColumnWidth),
        )
        TableHeaderCell(
            text = "Статус",
            modifier = Modifier.width(AdminTheme.Sizes.statusColumnWidth),
        )
    }
}

@Composable
private fun EmptyDayRow(
    onCreateClick: () -> Unit,
    row: UiState.Row.EmptyDay,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CellText(
            color = AdminTheme.Colors.muted,
            modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth),
            text = row.date,
        )
        CellText(
            color = AdminTheme.Colors.muted,
            modifier = Modifier.weight(1f),
            text = "—",
        )
        Spacer(modifier = Modifier.width(AdminTheme.Sizes.ledByColumnWidth + AdminTheme.Sizes.stepsColumnWidth))
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.statusColumnWidth)) {
            LinkButton(
                onClick = onCreateClick,
                text = "Создать",
            )
        }
    }
}

@Composable
private fun TopicRow(
    onTopicClick: (TopicId) -> Unit,
    row: UiState.Row.Topic,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CellText(
            color = AdminTheme.Colors.muted,
            modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth),
            text = row.date,
        )
        TableCell(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = { onTopicClick(row.topicId) },
                ),
                color = AdminTheme.Colors.ink,
                style = AdminTheme.Type.tableCell,
                fontWeight = FontWeight.Medium,
            )
        }
        CellText(
            color = AdminTheme.Colors.ink,
            modifier = Modifier.width(AdminTheme.Sizes.ledByColumnWidth),
            text = row.ledBy,
        )
        CellText(
            color = AdminTheme.Colors.muted,
            modifier = Modifier.width(AdminTheme.Sizes.stepsColumnWidth),
            text = row.steps,
        )
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.statusColumnWidth)) {
            StatusText(status = row.status)
        }
    }
}

@Composable
private fun CellText(
    color: Color,
    modifier: Modifier,
    text: String,
) {
    TableCell(modifier = modifier) {
        Text(
            text = text,
            color = color,
            style = AdminTheme.Type.tableCell,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
