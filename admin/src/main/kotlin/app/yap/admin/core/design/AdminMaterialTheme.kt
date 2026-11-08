package app.yap.admin.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun AdminMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = COLOR_SCHEME,
        shapes = SHAPES,
        typography = TYPOGRAPHY,
        content = content,
    )
}

private val COLOR_SCHEME = lightColorScheme(
    background = AdminTheme.Colors.background,
    error = AdminTheme.Colors.bad,
    inverseOnSurface = AdminTheme.Colors.background,
    inverseSurface = AdminTheme.Colors.ink,
    onBackground = AdminTheme.Colors.ink,
    onPrimary = AdminTheme.Colors.background,
    onSurface = AdminTheme.Colors.ink,
    onSurfaceVariant = AdminTheme.Colors.muted,
    outline = AdminTheme.Colors.lineStrong,
    outlineVariant = AdminTheme.Colors.line,
    primary = AdminTheme.Colors.ink,
    surface = AdminTheme.Colors.background,
    surfaceContainerHigh = AdminTheme.Colors.background,
)

private val CORNER = RoundedCornerShape(AdminTheme.Sizes.cornerRadius)

private val SHAPES = Shapes(
    extraSmall = CORNER,
    medium = CORNER,
    small = CORNER,
)

private val TYPOGRAPHY = Typography(
    bodyLarge = AdminTheme.Type.body,
    bodyMedium = AdminTheme.Type.body,
    bodySmall = AdminTheme.Type.small,
    labelLarge = AdminTheme.Type.body,
)
