package app.yap.admin.feature.characters.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.ScreenToolbar
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminTheme.Colors.background),
    ) {
        Column {
            CharacterContent(
                back = ToolbarBack(
                    label = "Персонажи",
                    onClick = onBackClick,
                ),
                onEvent = viewModel::onEvent,
                uiState = uiState,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun CharacterContent(
    back: ToolbarBack,
    onEvent: (Event) -> Unit,
    uiState: UiState,
) {
    when (uiState) {
        is UiState.Content -> CharacterForm(
            back = back,
            content = uiState,
            onEvent = onEvent,
        )
        is UiState.Error -> {
            ScreenToolbar(
                back = back,
                subtitle = "",
                title = "Персонаж",
            )
            Column(
                modifier = Modifier.padding(AdminTheme.Sizes.contentPadding),
                verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
            ) {
                Text(
                    text = uiState.message,
                    color = AdminTheme.Colors.ink,
                    fontSize = AdminTheme.Type.body,
                )
                OutlinedButton(onClick = { onEvent(Event.RetryClicked) }) {
                    Text("Повторить")
                }
            }
        }
        is UiState.Loading -> {
            ScreenToolbar(
                back = back,
                subtitle = "",
                title = "Персонаж",
            )
            CircularProgressIndicator(modifier = Modifier.padding(AdminTheme.Sizes.contentPadding))
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
        actions = {
            content.saveNote?.let { note ->
                Text(
                    text = note,
                    color = AdminTheme.Colors.muted,
                    fontSize = AdminTheme.Type.sub,
                )
            }
            Button(
                enabled = content.isSaveEnabled,
                onClick = { onEvent(Event.SaveClicked) },
            ) {
                Text("Сохранить")
            }
        },
        back = back,
        subtitle = content.subtitle,
        title = content.title,
    )
    Column(
        modifier = Modifier
            .widthIn(max = AdminTheme.Sizes.listMaxWidth)
            .verticalScroll(rememberScrollState())
            .padding(AdminTheme.Sizes.contentPadding),
        verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
    ) {
        content.hint?.let { hint ->
            Text(
                text = hint,
                color = AdminTheme.Colors.ink,
                fontSize = AdminTheme.Type.body,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = AdminTheme.Colors.fill,
                        shape = RoundedCornerShape(AdminTheme.Sizes.sideItemRadius),
                    )
                    .padding(AdminTheme.Sizes.rowPadding),
            )
        }
        Text(
            text = "Кто это",
            color = AdminTheme.Colors.ink,
            fontSize = AdminTheme.Type.sub,
            fontWeight = FontWeight.SemiBold,
        )
        ProfileField(
            label = "Имя — как его называют пользователи",
            onValueChange = { name -> onEvent(Event.FormChanged(content.form.copy(name = name))) },
            placeholder = "Например, Mia",
            value = content.form.name,
        )
        ProfileField(
            label = "История",
            onValueChange = { history -> onEvent(Event.FormChanged(content.form.copy(history = history))) },
            placeholder = "Кто он, откуда, чем живёт. Если он будет в разных ролях, объясните почему: " +
                "например, подрабатывает в разных местах.",
            value = content.form.history,
        )
        ProfileField(
            label = "Характер",
            onValueChange = { personality -> onEvent(Event.FormChanged(content.form.copy(personality = personality))) },
            placeholder = "Какой он в разговоре: тёплый, строгий, ворчливый, любопытный.",
            value = content.form.personality,
        )
        ProfileField(
            label = "Манера речи",
            onValueChange = { manner -> onEvent(Event.FormChanged(content.form.copy(manner = manner))) },
            placeholder = "Британский или американский, длинные или короткие фразы, есть ли сленг.",
            value = content.form.manner,
        )
        Text(
            text = "Роль в сцене — таксист, врач, сосед — задаётся в теме. " +
                "Сложность лексики задаётся в «Уровнях» и одна для всех персонажей.",
            color = AdminTheme.Colors.muted,
            fontSize = AdminTheme.Type.sub,
        )
    }
}

@Composable
private fun ProfileField(
    label: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    value: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
    )
}
