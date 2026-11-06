package app.yap.admin.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun ScreenToolbar(
    subtitle: String,
    title: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.toolbarHeight)
            .padding(
                horizontal = AdminTheme.Sizes.contentPadding,
                vertical = AdminTheme.Sizes.gap,
            ),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = AdminTheme.Colors.ink,
            fontSize = AdminTheme.Type.title,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            color = AdminTheme.Colors.muted,
            fontSize = AdminTheme.Type.sub,
        )
    }
    HorizontalDivider(color = AdminTheme.Colors.line)
}
