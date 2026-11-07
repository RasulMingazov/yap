package app.yap.admin.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object AdminTheme {

    object Colors {

        val background = Color(0xFFFFFFFF)
        val bad = Color(0xFFB4141A)
        val fill = Color(0xFFF4F4F5)
        val ink = Color(0xFF111113)
        val line = Color(0xFFECECEE)
        val lineStrong = Color(0xFFD9D9DD)
        val muted = Color(0xFF6B6B72)
        val selected = Color(0xFFEDEDF0)
        val side = Color(0xFFFAFAFA)
    }

    object Sizes {

        val brandGap = 6.dp
        val contentBottomPadding = 40.dp
        val contentMaxWidth = 880.dp
        val contentPadding = 32.dp
        val contentTopPadding = 24.dp
        val cornerRadius = 6.dp
        val fieldWidth = 360.dp
        val gap = 12.dp
        val hintPadding = 12.dp
        val rowMinHeight = 48.dp
        val rowPadding = 12.dp
        val sideBottomPadding = 16.dp
        val sideGroupGap = 20.dp
        val sideGroupTitlePadding = 6.dp
        val sideItemGap = 2.dp
        val sideItemHeight = 32.dp
        val sideItemPadding = 10.dp
        val sidePadding = 12.dp
        val sideWidth = 200.dp
        val toolbarHeight = 64.dp
        val toolbarTitleGap = 6.dp
        val windowHeight = 800.dp
        val windowWidth = 1280.dp
    }

    object Type {

        private val inter = FontFamily(
            Font(resource = "font/inter_regular.ttf", weight = FontWeight.Normal),
            Font(resource = "font/inter_semibold.ttf", weight = FontWeight.SemiBold),
        )

        val body = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp)
        val brand = TextStyle(fontFamily = inter, fontSize = 15.sp, lineHeight = 20.sp)
        val small = TextStyle(fontFamily = inter, fontSize = 12.sp, lineHeight = 16.sp)
        val sub = TextStyle(fontFamily = inter, fontSize = 13.sp, lineHeight = 18.sp)
        val title = TextStyle(fontFamily = inter, fontSize = 17.sp, lineHeight = 22.sp)
    }
}
