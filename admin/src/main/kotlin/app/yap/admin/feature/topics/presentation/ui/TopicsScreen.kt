package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
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
        ScreenBody(maxWidth = Dp.Unspecified) {
            when (uiState) {
                is UiState.Content -> TopicTable(
                    onTopicClick = onTopicClick,
                    rows = uiState.rows.filterIsInstance<UiState.Row.Topic>(),
                )
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
private fun TopicTable(
    onTopicClick: (TopicId) -> Unit,
    rows: List<UiState.Row.Topic>,
) {
    Column {
        Row {
            TableHeaderCell(
                text = "Дата",
                modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth),
            )
            TableHeaderCell(
                text = "Тема",
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = AdminTheme.Colors.line)
        rows.forEach { row ->
            key(row.topicId) {
                Row(
                    modifier = Modifier.clickable { onTopicClick(row.topicId) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableCell(modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth)) {
                        Text(
                            text = row.date,
                            color = AdminTheme.Colors.muted,
                            style = AdminTheme.Type.tableCell,
                        )
                    }
                    TableCell(modifier = Modifier.weight(1f)) {
                        Text(
                            text = row.title,
                            color = AdminTheme.Colors.ink,
                            style = AdminTheme.Type.tableCell,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
    }
}
