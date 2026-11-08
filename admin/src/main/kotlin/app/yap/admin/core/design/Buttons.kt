package app.yap.admin.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Composable
internal fun PrimaryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    text: String,
) {
    ButtonBox(
        background = if (enabled) AdminTheme.Colors.ink else AdminTheme.Colors.fill,
        border = if (enabled) AdminTheme.Colors.ink else AdminTheme.Colors.lineStrong,
        enabled = enabled,
        horizontalPadding = AdminTheme.Sizes.buttonPadding,
        onClick = onClick,
    ) {
        ButtonText(
            color = if (enabled) AdminTheme.Colors.background else AdminTheme.Colors.muted,
            text = text,
        )
    }
}

@Composable
internal fun SecondaryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    text: String,
) {
    ButtonBox(
        background = if (enabled) AdminTheme.Colors.background else AdminTheme.Colors.fill,
        border = AdminTheme.Colors.lineStrong,
        enabled = enabled,
        horizontalPadding = AdminTheme.Sizes.secondaryButtonPadding,
        onClick = onClick,
    ) {
        ButtonText(
            color = if (enabled) AdminTheme.Colors.ink else AdminTheme.Colors.muted,
            text = text,
        )
    }
}

@Composable
internal fun PopupButton(
    enabled: Boolean,
    onClick: () -> Unit,
    placeholder: String,
    text: String?,
) {
    ButtonBox(
        background = if (enabled) AdminTheme.Colors.background else AdminTheme.Colors.fill,
        border = AdminTheme.Colors.lineStrong,
        enabled = enabled,
        horizontalPadding = AdminTheme.Sizes.popupPadding,
        onClick = onClick,
    ) {
        Text(
            text = text ?: placeholder,
            color = if (enabled && text != null) AdminTheme.Colors.ink else AdminTheme.Colors.muted,
            style = AdminTheme.Type.body,
        )
    }
}

@Composable
internal fun LinkButton(
    onClick: () -> Unit,
    text: String,
) {
    var baseline by remember { mutableFloatStateOf(0f) }
    Text(
        text = text,
        color = AdminTheme.Colors.ink,
        style = AdminTheme.Type.body,
        fontWeight = FontWeight.Medium,
        onTextLayout = { layout -> baseline = layout.firstBaseline },
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .drawBehind {
                val underline = baseline + AdminTheme.Sizes.linkUnderlineOffset.toPx()
                drawLine(
                    color = AdminTheme.Colors.lineStrong,
                    start = Offset(x = 0f, y = underline),
                    end = Offset(x = size.width, y = underline),
                    strokeWidth = AdminTheme.Sizes.border.toPx(),
                )
            },
    )
}

@Composable
private fun ButtonBox(
    background: Color,
    border: Color,
    enabled: Boolean,
    horizontalPadding: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(AdminTheme.Sizes.cornerRadius)
    Box(
        modifier = Modifier
            .height(AdminTheme.Sizes.buttonHeight)
            .clip(shape)
            .background(background)
            .border(
                width = AdminTheme.Sizes.border,
                color = border,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ButtonText(
    color: Color,
    text: String,
) {
    Text(
        text = text,
        color = color,
        style = AdminTheme.Type.body,
        fontWeight = FontWeight.Medium,
    )
}
