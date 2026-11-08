package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.Hint
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.SecondaryButton
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.core.design.ToolbarBack
import app.yap.admin.core.design.ToolbarSaveAction
import app.yap.admin.feature.topics.presentation.TopicViewModel
import app.yap.admin.feature.topics.presentation.TopicViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicViewModel.News
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState

@Composable
internal fun TopicScreen(
    onBackClick: () -> Unit,
    viewModel: TopicViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.news.collect { news ->
            when (news) {
                is News.ShowSnackbar -> snackbarHostState.showSnackbar(news.message)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        TopicContent(
            back = ToolbarBack(
                label = "Темы",
                onClick = onBackClick,
            ),
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
internal fun TopicContent(
    back: ToolbarBack,
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        when (uiState) {
            is UiState.Content -> TopicForm(
                back = back,
                content = uiState,
                onEvent = onEvent,
            )
            is UiState.Error -> {
                PlaceholderToolbar(
                    back = back,
                    toolbar = uiState.toolbar,
                )
                ScreenBody {
                    LoadError(
                        message = uiState.message,
                        onRetryClick = { onEvent(Event.RetryClicked) },
                    )
                }
            }
            is UiState.Loading -> {
                PlaceholderToolbar(
                    back = back,
                    toolbar = uiState.toolbar,
                )
                ScreenBody {
                    Loader()
                }
            }
        }
    }
}

@Composable
private fun TopicForm(
    back: ToolbarBack,
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    ScreenToolbar(
        actions = {
            ToolbarSaveAction(status = content.saveStatus) {
                SecondaryButton(
                    enabled = content.isSaveEnabled,
                    onClick = { onEvent(Event.SaveClicked) },
                    text = "Сохранить черновик",
                )
            }
        },
        back = back.copy(enabled = content.isSaveEnabled),
        subtitle = content.subtitle,
        title = content.title,
    )
    ScreenBody(
        maxWidth = Dp.Unspecified,
        spacing = AdminTheme.Sizes.sectionGap,
    ) {
        content.hint?.let { hint -> Hint(text = hint) }
        SceneSection(
            content = content,
            onEvent = onEvent,
        )
        InterlocutorSection(
            content = content,
            onEvent = onEvent,
        )
        OpeningLineSection(
            content = content,
            onEvent = onEvent,
        )
        StepsSection(
            content = content,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun SceneSection(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    val form = content.form
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        SectionHeader(
            number = 1,
            title = "Сцена",
        )
        AdminTextField(
            label = "Название",
            onValueChange = { title -> onEvent(Event.FormChanged(form.copy(title = title))) },
            value = form.title,
            enabled = content.isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        ReleaseDateField(
            date = form.releaseDate,
            enabled = content.isSaveEnabled,
            onDatePick = { date -> onEvent(Event.ReleaseDatePicked(date)) },
            text = content.releaseDateText,
        )
        AdminTextField(
            label = "Длительность, мин",
            onValueChange = { text -> onEvent(Event.DurationChanged(text)) },
            value = form.durationMinutes?.toString().orEmpty(),
            enabled = content.isSaveEnabled,
        )
        AdminTextField(
            label = "Описание — одна строка на карточке дня",
            onValueChange = { description -> onEvent(Event.FormChanged(form.copy(description = description))) },
            value = form.description,
            enabled = content.isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        AdminTextField(
            label = "Обстановка — где, когда и что вокруг. Пользователь её не видит, по ней играет модель",
            onValueChange = { setting -> onEvent(Event.FormChanged(form.copy(setting = setting))) },
            value = form.setting,
            enabled = content.isSaveEnabled,
            minLines = SETTING_LINES,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OpeningLineSection(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    val form = content.form
    val openingLine = form.openingLine
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        SectionHeader(
            number = 3,
            title = "Первая реплика",
        )
        AdminTextField(
            label = "По-английски",
            onValueChange = { english ->
                onEvent(Event.FormChanged(form.copy(openingLine = openingLine.copy(english = english))))
            },
            value = openingLine.english,
            enabled = content.isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        AdminTextField(
            label = "Перевод",
            onValueChange = { translation ->
                onEvent(Event.FormChanged(form.copy(openingLine = openingLine.copy(translation = translation))))
            },
            value = openingLine.translation,
            enabled = content.isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PlaceholderToolbar(
    back: ToolbarBack,
    toolbar: UiState.Toolbar,
) {
    ScreenToolbar(
        back = back,
        subtitle = toolbar.subtitle,
        title = toolbar.title,
    )
}

private const val SETTING_LINES = 3
