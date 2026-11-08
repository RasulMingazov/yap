package app.yap.admin.feature.models.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminSelect
import app.yap.admin.core.design.AdminTheme
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
import app.yap.admin.core.design.ToolbarSaveAction
import app.yap.admin.feature.models.presentation.ModelsViewModel
import app.yap.admin.feature.models.presentation.ModelsViewModel.Event
import app.yap.admin.feature.models.presentation.ModelsViewModel.News
import app.yap.admin.feature.models.presentation.ModelsViewModel.UiState

@Composable
internal fun ModelsScreen(viewModel: ModelsViewModel) {
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
        ModelsContent(
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
internal fun ModelsContent(
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        when (uiState) {
            is UiState.Content -> {
                ModelsToolbar {
                    ToolbarSaveAction(status = uiState.saveStatus) {
                        if (uiState.isRefreshShown) {
                            SecondaryButton(
                                enabled = true,
                                onClick = { onEvent(Event.RefreshClicked) },
                                text = "Обновить",
                            )
                        }
                        PrimaryButton(
                            enabled = uiState.isFormEnabled,
                            onClick = { onEvent(Event.SaveClicked) },
                            text = "Сохранить",
                        )
                    }
                }
                ModelsForm(
                    content = uiState,
                    onEvent = onEvent,
                )
            }
            is UiState.Error -> {
                ModelsToolbar()
                ScreenBody {
                    LoadError(
                        message = uiState.message,
                        onRetryClick = { onEvent(Event.RetryClicked) },
                    )
                }
            }
            is UiState.Loading -> {
                ModelsToolbar()
                ScreenBody {
                    Loader()
                }
            }
        }
    }
}

@Composable
private fun ModelsToolbar(actions: @Composable RowScope.() -> Unit = {}) {
    ScreenToolbar(
        actions = actions,
        subtitle = "Какая модель за что отвечает",
        title = "Модели",
    )
}

@Composable
private fun ModelsForm(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    ScreenBody(
        maxWidth = AdminTheme.Sizes.modelsMaxWidth,
        spacing = AdminTheme.Sizes.sectionGap,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
            SectionHeader(title = "Кто за что отвечает")
            TaskTable(
                content = content,
                onEvent = onEvent,
            )
            Note(text = TASKS_NOTE)
        }
        Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionInnerGap)) {
            SectionHeader(title = "Провайдеры")
            Column {
                HorizontalDivider(color = AdminTheme.Colors.line)
                content.providers.forEach { provider ->
                    key(provider.name) {
                        ProviderRow(provider = provider)
                        HorizontalDivider(color = AdminTheme.Colors.line)
                    }
                }
            }
            Note(text = PROVIDERS_NOTE)
        }
    }
}

@Composable
private fun TaskTable(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    Column {
        Row {
            TableHeaderCell(
                text = "Задача",
                modifier = Modifier.weight(1f),
            )
            TableHeaderCell(
                text = "Провайдер",
                modifier = Modifier.width(AdminTheme.Sizes.modelProviderColumnWidth),
            )
            TableHeaderCell(
                text = "Модель",
                modifier = Modifier.width(AdminTheme.Sizes.modelColumnWidth),
            )
        }
        HorizontalDivider(color = AdminTheme.Colors.line)
        content.tasks.forEach { row ->
            key(row.task) {
                TaskRow(
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
private fun TaskRow(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
    row: UiState.TaskRow,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TableCell(modifier = Modifier.weight(1f)) {
            Column {
                Text(
                    text = row.title,
                    color = AdminTheme.Colors.ink,
                    style = AdminTheme.Type.tableCellMedium,
                )
                Text(
                    text = row.usage,
                    color = AdminTheme.Colors.muted,
                    style = AdminTheme.Type.sub,
                )
            }
        }
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.modelProviderColumnWidth)) {
            Column {
                AdminSelect(
                    enabled = content.isFormEnabled,
                    label = { option -> option.name },
                    onSelect = { option -> onEvent(Event.ProviderChanged(option.code, row.task)) },
                    options = content.providerOptions,
                    placeholder = "Выберите",
                    value = row.provider,
                    height = AdminTheme.Sizes.buttonHeight,
                )
                row.mark?.let { mark -> StatusText(status = mark) }
            }
        }
        TableCell(modifier = Modifier.width(AdminTheme.Sizes.modelColumnWidth)) {
            AdminSelect(
                enabled = content.isFormEnabled,
                label = { model -> model },
                onSelect = { model -> onEvent(Event.ModelChanged(model, row.task)) },
                options = row.models,
                placeholder = "Выберите",
                value = row.model,
                height = AdminTheme.Sizes.buttonHeight,
            )
        }
    }
}

@Composable
private fun ProviderRow(provider: UiState.ProviderRow) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.rowGap),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.rowMinHeight)
            .padding(vertical = AdminTheme.Sizes.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = provider.name,
                color = AdminTheme.Colors.ink,
                style = AdminTheme.Type.bodyMedium,
            )
            Text(
                text = provider.models,
                color = AdminTheme.Colors.muted,
                style = AdminTheme.Type.sub,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            StatusText(status = provider.state)
            provider.step?.let { step -> Note(text = step) }
        }
    }
}

private const val PROVIDERS_NOTE = "Ключи хранятся на сервере, админка их не показывает."

private const val TASKS_NOTE =
    "Для реплики персонажа и подсказок уровень рейтинга может задать свою модель в «Уровнях». " +
        "Если там стоит «по умолчанию», работает модель из этой таблицы."
