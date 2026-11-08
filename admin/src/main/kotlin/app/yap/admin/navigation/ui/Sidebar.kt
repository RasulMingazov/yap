package app.yap.admin.navigation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .padding(
                start = AdminTheme.Sizes.sidePadding,
                end = AdminTheme.Sizes.sidePadding,
                bottom = AdminTheme.Sizes.sideBottomPadding,
            ),
    ) {
        Brand()
        SidebarGroup(title = "Контент") {
            SidebarItem(
                current = current,
                label = "Темы",
                onScreenClick = onScreenClick,
                screen = Screen.Topics,
            )
            SidebarItem(
                current = current,
                label = "Персонажи",
                onScreenClick = onScreenClick,
                screen = Screen.Characters,
            )
        }
        SidebarGroup(title = "Правила") {
            SidebarItem(
                current = current,
                label = "Уровни",
                onScreenClick = onScreenClick,
                screen = Screen.Levels,
            )
            SidebarItem(
                current = current,
                label = "Промпты",
                onScreenClick = onScreenClick,
                screen = Screen.Prompts,
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
            style = AdminTheme.Type.small,
            modifier = Modifier.padding(horizontal = AdminTheme.Sizes.sideItemPadding),
        )
    }
}

@Composable
private fun Brand() {
    Row(
        modifier = Modifier
            .height(AdminTheme.Sizes.toolbarHeight)
            .padding(horizontal = AdminTheme.Sizes.sideItemPadding),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.brandGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Yap",
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.brand,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "admin",
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.brand,
        )
    }
}

@Composable
private fun SidebarGroup(
    title: String,
    items: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = AdminTheme.Sizes.sideGroupGap)) {
        Text(
            text = title,
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.small,
            modifier = Modifier.padding(
                start = AdminTheme.Sizes.sideItemPadding,
                end = AdminTheme.Sizes.sideItemPadding,
                bottom = AdminTheme.Sizes.sideGroupTitlePadding,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sideItemGap)) {
            items()
        }
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
        style = AdminTheme.Type.body,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(AdminTheme.Sizes.sideItemHeight)
            .clip(RoundedCornerShape(AdminTheme.Sizes.cornerRadius))
            .background(if (isCurrent) AdminTheme.Colors.selected else Color.Transparent)
            .clickable { onScreenClick(screen) }
            .padding(horizontal = AdminTheme.Sizes.sideItemPadding)
            .wrapContentHeight(Alignment.CenterVertically),
    )
}
