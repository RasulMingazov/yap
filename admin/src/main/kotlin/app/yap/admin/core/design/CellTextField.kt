package app.yap.admin.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
internal fun CellTextField(
    enabled: Boolean,
    label: String,
    onValueChange: (String) -> Unit,
    value: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(AdminTheme.Sizes.cornerRadius)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(AdminTheme.Sizes.cellFieldHeight)
            .semantics { contentDescription = label },
        enabled = enabled,
        textStyle = AdminTheme.Type.body.copy(color = if (enabled) AdminTheme.Colors.ink else AdminTheme.Colors.muted),
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(AdminTheme.Colors.ink),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (enabled) AdminTheme.Colors.background else AdminTheme.Colors.fill,
                        shape = shape,
                    )
                    .border(
                        width = if (isFocused) AdminTheme.Sizes.focusBorder else AdminTheme.Sizes.border,
                        color = if (isFocused) AdminTheme.Colors.ink else AdminTheme.Colors.lineStrong,
                        shape = shape,
                    )
                    .padding(horizontal = AdminTheme.Sizes.cellFieldPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                innerTextField()
            }
        },
    )
}
