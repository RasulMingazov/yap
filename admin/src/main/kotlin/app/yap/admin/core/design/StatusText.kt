package app.yap.admin.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import app.yap.admin.core.presentation.Status

@Composable
internal fun StatusText(
    status: Status,
    modifier: Modifier = Modifier,
    style: TextStyle = AdminTheme.Type.sub,
) {
    Text(
        text = status.text,
        modifier = modifier,
        color = when (status.tone) {
            Status.Tone.ERROR -> AdminTheme.Colors.bad
            Status.Tone.MUTED -> AdminTheme.Colors.muted
            Status.Tone.SUCCESS -> AdminTheme.Colors.ok
            Status.Tone.WARNING -> AdminTheme.Colors.warn
        },
        style = style,
    )
}
