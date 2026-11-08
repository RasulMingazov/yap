package app.yap.admin.core.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Composable
internal fun ScreenToolbar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    back: ToolbarBack? = null,
    subtitle: String? = null,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.toolbarHeight)
            .padding(
                horizontal = AdminTheme.Sizes.contentPadding,
                vertical = AdminTheme.Sizes.gap,
            ),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.toolbarActionsGap),
        verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = AdminTheme.Sizes.toolbarTitleMinWidth),
            horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.toolbarTitleGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            back?.let { toolbarBack -> BackButton(toolbarBack = toolbarBack) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AdminTheme.Colors.ink,
                    style = AdminTheme.Type.title,
                    fontWeight = FontWeight.SemiBold,
                )
                subtitle?.let { text ->
                    Text(
                        text = text,
                        color = AdminTheme.Colors.muted,
                        style = AdminTheme.Type.sub,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
    HorizontalDivider(color = AdminTheme.Colors.line)
}

internal data class ToolbarBack(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

@Composable
private fun BackButton(toolbarBack: ToolbarBack) {
    Box(
        modifier = Modifier
            .pullStart(AdminTheme.Sizes.backPull)
            .size(AdminTheme.Sizes.backButton)
            .clip(RoundedCornerShape(AdminTheme.Sizes.cornerRadius))
            .clickable(
                enabled = toolbarBack.enabled,
                role = Role.Button,
                onClick = toolbarBack.onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BACK_ARROW,
            contentDescription = "Назад: ${toolbarBack.label}",
            tint = if (toolbarBack.enabled) AdminTheme.Colors.ink else AdminTheme.Colors.muted,
            modifier = Modifier.size(AdminTheme.Sizes.backArrow),
        )
    }
}

private fun Modifier.pullStart(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val pull = amount.roundToPx()
    layout(width = placeable.width - pull, height = placeable.height) {
        placeable.place(x = -pull, y = 0)
    }
}

private val BACK_ARROW = ImageVector
    .Builder(
        name = "BackArrow",
        defaultWidth = AdminTheme.Sizes.backArrow,
        defaultHeight = AdminTheme.Sizes.backArrow,
        viewportWidth = 20f,
        viewportHeight = 20f,
    )
    .path(
        stroke = SolidColor(AdminTheme.Colors.ink),
        strokeLineWidth = 1.75f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) {
        moveTo(15.5f, 10f)
        horizontalLineTo(4.5f)
        moveTo(9.5f, 5f)
        lineTo(4.5f, 10f)
        lineTo(9.5f, 15f)
    }
    .build()
