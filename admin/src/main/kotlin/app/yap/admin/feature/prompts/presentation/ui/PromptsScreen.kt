package app.yap.admin.feature.prompts.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.ToolbarSaveAction
import app.yap.admin.feature.prompts.presentation.PromptsViewModel
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Event
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.News
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState

@Composable
internal fun PromptsScreen(viewModel: PromptsViewModel) {
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
        PromptsContent(
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
internal fun PromptsContent(
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        when (uiState) {
            is UiState.Content -> {
                PromptsToolbar {
                    ToolbarSaveAction(status = uiState.saveStatus) {
                        PrimaryButton(
                            enabled = uiState.isSaveEnabled,
                            onClick = { onEvent(Event.SaveClicked) },
                            text = "Сохранить",
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    PromptList(
                        content = uiState,
                        onEvent = onEvent,
                    )
                    VerticalDivider(color = AdminTheme.Colors.line)
                    Box(modifier = Modifier.weight(1f)) {
                        PromptEditor(
                            editor = uiState.editor,
                            onEvent = onEvent,
                        )
                    }
                }
            }
            is UiState.Error -> {
                PromptsToolbar()
                ScreenBody {
                    LoadError(
                        message = uiState.message,
                        onRetryClick = { onEvent(Event.RetryClicked) },
                    )
                }
            }
            is UiState.Loading -> {
                PromptsToolbar()
                ScreenBody {
                    Loader()
                }
            }
        }
    }
}

@Composable
private fun PromptsToolbar(actions: @Composable RowScope.() -> Unit = {}) {
    ScreenToolbar(
        actions = actions,
        subtitle = "Влияет на все темы",
        title = "Промпты",
    )
}

@Composable
private fun PromptList(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(AdminTheme.Sizes.promptListWidth)
            .fillMaxHeight()
            .padding(
                horizontal = AdminTheme.Sizes.promptListHorizontalPadding,
                vertical = AdminTheme.Sizes.promptListVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.promptGroupGap),
    ) {
        content.groups.forEach { group ->
            key(group.title) {
                PromptListGroup(
                    enabled = content.isListEnabled,
                    group = group,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun PromptListGroup(
    enabled: Boolean,
    group: UiState.Group,
    onEvent: (Event) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sideItemGap)) {
        Text(
            text = group.title,
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.small,
            modifier = Modifier.padding(
                start = AdminTheme.Sizes.sideItemPadding,
                end = AdminTheme.Sizes.sideItemPadding,
                bottom = AdminTheme.Sizes.sideGroupTitlePadding,
            ),
        )
        group.items.forEach { item ->
            key(item.promptKey) {
                PromptListItem(
                    enabled = enabled,
                    item = item,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun PromptListItem(
    enabled: Boolean,
    item: UiState.Item,
    onEvent: (Event) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdminTheme.Sizes.cornerRadius))
            .background(if (item.isChosen) AdminTheme.Colors.selected else AdminTheme.Colors.transparent)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = { onEvent(Event.PromptClicked(item.promptKey)) },
            )
            .padding(
                horizontal = AdminTheme.Sizes.sideItemPadding,
                vertical = AdminTheme.Sizes.promptItemVerticalPadding,
            ),
    ) {
        Text(
            text = item.title,
            color = if (enabled) AdminTheme.Colors.ink else AdminTheme.Colors.muted,
            style = AdminTheme.Type.body,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = item.usage,
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.sub,
        )
    }
}

@Composable
private fun PromptEditor(
    editor: UiState.Editor,
    onEvent: (Event) -> Unit,
) {
    when (editor) {
        is UiState.Editor.Error -> ScreenBody {
            LoadError(
                message = editor.message,
                onRetryClick = { onEvent(Event.RetryClicked) },
            )
        }
        is UiState.Editor.Loaded -> LoadedEditor(
            editor = editor,
            onEvent = onEvent,
        )
        is UiState.Editor.Loading -> ScreenBody {
            Loader()
        }
    }
}

@Composable
private fun LoadedEditor(
    editor: UiState.Editor.Loaded,
    onEvent: (Event) -> Unit,
) {
    ScreenBody(maxWidth = Dp.Unspecified) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = editor.title,
                color = AdminTheme.Colors.ink,
                style = AdminTheme.Type.heading,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = editor.state,
                color = AdminTheme.Colors.muted,
                style = AdminTheme.Type.sub,
                modifier = Modifier.alignByBaseline(),
            )
        }
        AdminTextField(
            label = "Текст промпта",
            onValueChange = { text -> onEvent(Event.TextChanged(text)) },
            value = editor.text,
            enabled = editor.isEditable,
            minLines = TEXT_LINES,
            modifier = Modifier.fillMaxWidth(),
            textStyle = AdminTheme.Type.mono,
        )
    }
}

private const val TEXT_LINES = 16
