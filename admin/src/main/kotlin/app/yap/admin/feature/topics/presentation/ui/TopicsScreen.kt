package app.yap.admin.feature.topics.presentation.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.TableCell
import app.yap.admin.core.design.TableHeaderCell
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import app.yap.admin.feature.topics.presentation.TopicsViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicsViewModel.UiState

@Composable
internal fun TopicsScreen(viewModel: TopicsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.onEvent(Event.Opened)
    }
    TopicsContent(
        onEvent = viewModel::onEvent,
        uiState = uiState,
    )
}

@Composable
internal fun TopicsContent(
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenToolbar(
            subtitle = "Одна тема в день",
            title = "Темы",
        )
        ScreenBody {
            when (uiState) {
                is UiState.Content -> TopicTable(rows = uiState.rows)
                is UiState.Empty -> Text(
                    text = uiState.message,
                    color = AdminTheme.Colors.muted,
                    style = AdminTheme.Type.body,
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
private fun TopicTable(rows: List<UiState.Content.Row>) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TableCell(modifier = Modifier.width(AdminTheme.Sizes.dateColumnWidth)) {
                        Text(
                            text = row.date,
                            color = AdminTheme.Colors.muted,
                            style = AdminTheme.Type.body,
                        )
                    }
                    TableCell(modifier = Modifier.weight(1f)) {
                        Text(
                            text = row.title,
                            color = AdminTheme.Colors.ink,
                            style = AdminTheme.Type.body,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
    }
}
