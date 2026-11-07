package app.yap.admin.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.yap.admin.core.presentation.Status

@Composable
internal fun StatusText(status: Status) {
    Text(
        text = status.text,
        color = when (status.tone) {
            Status.Tone.ERROR -> AdminTheme.Colors.bad
            Status.Tone.SUCCESS -> AdminTheme.Colors.ok
        },
        style = AdminTheme.Type.sub,
    )
}
