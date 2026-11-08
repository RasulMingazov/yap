package app.yap.admin.feature.characters.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.Hint
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.Loader
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.core.design.StatusText
import app.yap.admin.core.design.ToolbarBack
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharacterViewModel.Event
import app.yap.admin.feature.characters.presentation.CharacterViewModel.News
import app.yap.admin.feature.characters.presentation.CharacterViewModel.UiState

@Composable
internal fun CharacterScreen(
    onBackClick: () -> Unit,
    viewModel: CharacterViewModel,
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
        CharacterContent(
            back = ToolbarBack(
                label = "Персонажи",
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
internal fun CharacterContent(
    back: ToolbarBack,
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    Column {
        when (uiState) {
            is UiState.Content -> CharacterForm(
                back = back,
                content = uiState,
                onEvent = onEvent,
            )
            is UiState.Error -> {
                PlaceholderToolbar(back = back)
                ScreenBody {
                    LoadError(
                        message = uiState.message,
                        onRetryClick = { onEvent(Event.RetryClicked) },
                    )
                }
            }
            is UiState.Loading -> {
                PlaceholderToolbar(back = back)
                ScreenBody {
                    Loader()
                }
            }
        }
    }
}

@Composable
private fun CharacterForm(
    back: ToolbarBack,
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    ScreenToolbar(
        back = back,
        subtitle = content.subtitle,
        title = content.title,
    )
    ScreenBody(spacing = AdminTheme.Sizes.sectionGap) {
        content.hint?.let { hint -> Hint(text = hint) }
        Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
            SectionHeader(title = "Кто это")
            AdminTextField(
                label = "Имя — как его называют пользователи",
                onValueChange = { name -> onEvent(Event.FormChanged(content.form.copy(name = name))) },
                value = content.form.name,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Например, Mia",
            )
            AdminTextField(
                label = "История",
                onValueChange = { history -> onEvent(Event.FormChanged(content.form.copy(history = history))) },
                value = content.form.history,
                minLines = PROFILE_LINES,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Кто он, откуда, чем живёт. Если он будет в разных ролях, объясните почему: " +
                    "например, подрабатывает в разных местах.",
            )
            AdminTextField(
                label = "Характер",
                onValueChange = { personality -> onEvent(Event.FormChanged(content.form.copy(personality = personality))) },
                value = content.form.personality,
                minLines = PROFILE_LINES,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Какой он в разговоре: тёплый, строгий, ворчливый, любопытный.",
            )
            AdminTextField(
                label = "Манера речи",
                onValueChange = { manner -> onEvent(Event.FormChanged(content.form.copy(manner = manner))) },
                value = content.form.manner,
                minLines = PROFILE_LINES,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Британский или американский, длинные или короткие фразы, есть ли сленг.",
            )
            Note(
                text = "Роль в сцене — таксист, врач, сосед — задаётся в теме. " +
                    "Сложность лексики задаётся в «Уровнях» и одна для всех персонажей.",
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrimaryButton(
                enabled = content.isSaveEnabled,
                onClick = { onEvent(Event.SaveClicked) },
                text = "Сохранить",
            )
            content.saveStatus?.let { status -> StatusText(status = status) }
        }
    }
}

@Composable
private fun PlaceholderToolbar(back: ToolbarBack) {
    ScreenToolbar(
        back = back,
        title = "Персонаж",
    )
}

private const val PROFILE_LINES = 2
