package app.yap.admin.core.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun ScreenToolbar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    back: ToolbarBack? = null,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.toolbarHeight)
            .padding(
                horizontal = AdminTheme.Sizes.contentPadding,
                vertical = AdminTheme.Sizes.gap,
            ),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.toolbarTitleGap)) {
                back?.let { toolbarBack ->
                    Text(
                        text = toolbarBack.label,
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.title,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClick = toolbarBack.onClick,
                        ),
                    )
                    Text(
                        text = "/",
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.title,
                    )
                }
                Text(
                    text = title,
                    color = AdminTheme.Colors.ink,
                    style = AdminTheme.Type.title,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            subtitle?.let { text ->
                Text(
                    text = text,
                    color = AdminTheme.Colors.muted,
                    style = AdminTheme.Type.sub,
                )
            }
        }
        actions()
    }
    HorizontalDivider(color = AdminTheme.Colors.line)
}

internal data class ToolbarBack(
    val label: String,
    val onClick: () -> Unit,
)
