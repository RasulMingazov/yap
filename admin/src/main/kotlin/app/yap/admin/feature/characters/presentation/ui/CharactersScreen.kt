package app.yap.admin.feature.characters.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LoadError
import app.yap.admin.core.design.ScreenBody
import app.yap.admin.core.design.ScreenToolbar
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.characters.presentation.CharactersViewModel.Event
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState

@Composable
internal fun CharactersScreen(
    onCharacterClick: (CharacterId) -> Unit,
    onNewCharacterClick: () -> Unit,
    viewModel: CharactersViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharactersContent(
        onCharacterClick = onCharacterClick,
        onEvent = viewModel::onEvent,
        onNewCharacterClick = onNewCharacterClick,
        uiState = uiState,
    )
}

@Composable
internal fun CharactersContent(
    onCharacterClick: (CharacterId) -> Unit,
    onEvent: (Event) -> Unit,
    onNewCharacterClick: () -> Unit,
    uiState: UiState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenToolbar(
            subtitle = "Постоянные герои, как в ситкоме",
            title = "Персонажи",
        )
        ScreenBody {
            when (uiState) {
                is UiState.Content -> {
                    CharacterRows(
                        onCharacterClick = onCharacterClick,
                        rows = uiState.rows,
                    )
                    NewCharacterButton(onClick = onNewCharacterClick)
                }
                is UiState.Empty -> {
                    Text(
                        text = uiState.message,
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.body,
                    )
                    NewCharacterButton(onClick = onNewCharacterClick)
                }
                is UiState.Error -> LoadError(
                    message = uiState.message,
                    onRetryClick = { onEvent(Event.RetryClicked) },
                )
                is UiState.Loading -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CharacterRows(
    onCharacterClick: (CharacterId) -> Unit,
    rows: List<UiState.Content.Row>,
) {
    Column {
        HorizontalDivider(color = AdminTheme.Colors.line)
        rows.forEach { row ->
            key(row.characterId) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCharacterClick(row.characterId) }
                        .heightIn(min = AdminTheme.Sizes.rowMinHeight)
                        .padding(vertical = AdminTheme.Sizes.rowPadding),
                ) {
                    Text(
                        text = row.name,
                        color = AdminTheme.Colors.ink,
                        style = AdminTheme.Type.body,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = row.personality,
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.sub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
    }
}

@Composable
private fun NewCharacterButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = "Новый персонаж",
            color = AdminTheme.Colors.ink,
            textDecoration = TextDecoration.Underline,
        )
    }
}
