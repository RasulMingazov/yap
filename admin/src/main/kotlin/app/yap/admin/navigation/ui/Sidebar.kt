package app.yap.admin.navigation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.navigation.NavigationViewModel.Screen

@Composable
internal fun Sidebar(
    current: Screen,
    onScreenClick: (Screen) -> Unit,
    serverLabel: String,
) {
    Column(
        modifier = Modifier
            .width(AdminTheme.Sizes.sideWidth)
            .fillMaxHeight()
            .background(AdminTheme.Colors.side)
            .padding(AdminTheme.Sizes.sidePadding),
        verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
    ) {
        Text(
            text = "Yap admin",
            color = AdminTheme.Colors.ink,
            fontWeight = FontWeight.SemiBold,
        )
        SidebarGroup(title = "Контент") {
            SidebarItem(
                current = current,
                label = "Персонажи",
                onScreenClick = onScreenClick,
                screen = Screen.Characters,
            )
        }
        SidebarGroup(title = "Настройки") {
            SidebarItem(
                current = current,
                label = "Сервер",
                onScreenClick = onScreenClick,
                screen = Screen.Server,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = serverLabel,
            color = AdminTheme.Colors.muted,
            fontSize = AdminTheme.Type.small,
        )
    }
}

@Composable
private fun SidebarGroup(
    title: String,
    items: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            color = AdminTheme.Colors.muted,
            fontSize = AdminTheme.Type.small,
        )
        items()
    }
}

@Composable
private fun SidebarItem(
    current: Screen,
    label: String,
    onScreenClick: (Screen) -> Unit,
    screen: Screen,
) {
    val isCurrent = screen == current
    Text(
        text = label,
        color = if (isCurrent) AdminTheme.Colors.ink else AdminTheme.Colors.muted,
        fontSize = AdminTheme.Type.body,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdminTheme.Sizes.cornerRadius))
            .background(if (isCurrent) AdminTheme.Colors.selected else Color.Transparent)
            .clickable { onScreenClick(screen) }
            .padding(AdminTheme.Sizes.sideItemPadding),
    )
}
