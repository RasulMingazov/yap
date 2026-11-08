package app.yap.admin.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.LineHeightStyle
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
        val ok = Color(0xFF1F7A3F)
        val radioRing = Color(0xFFB8B8BE)
        val selected = Color(0xFFEDEDF0)
        val side = Color(0xFFFAFAFA)
        val transparent = Color.Transparent
    }

    object Sizes {

        val backArrow = 20.dp
        val backButton = 32.dp
        val backPull = 8.dp
        val border = 1.dp
        val brandGap = 6.dp
        val buttonHeight = 32.dp
        val buttonPadding = 14.dp
        val cellFieldHeight = 28.dp
        val cellFieldPadding = 10.dp
        val cellFieldWidth = 64.dp
        val contentBottomPadding = 40.dp
        val contentMaxWidth = 880.dp
        val contentPadding = 32.dp
        val contentTopPadding = 24.dp
        val cornerRadius = 6.dp
        val dateColumnWidth = 110.dp
        val dialogPadding = 24.dp
        val fieldWidth = 360.dp
        val focusBorder = 2.dp
        val gap = 12.dp
        val hintHorizontalPadding = 14.dp
        val hintVerticalPadding = 12.dp
        val levelCodeColumnWidth = 120.dp
        val levelColumnWidth = 110.dp
        val levelWordsColumnWidth = 130.dp
        val linkUnderlineOffset = 3.dp
        val loader = 20.dp
        val loaderStroke = 2.dp
        val popupPadding = 8.dp
        val promptGroupGap = 16.dp
        val promptItemVerticalPadding = 8.dp
        val promptListHorizontalPadding = 12.dp
        val promptListVerticalPadding = 16.dp
        val promptListWidth = 220.dp
        val radio = 14.dp
        val radioInset = 4.dp
        val rowGap = 16.dp
        val rowMinHeight = 48.dp
        val rowPadding = 12.dp
        val secondaryButtonPadding = 12.dp
        val sectionGap = 32.dp
        val sectionInnerGap = 8.dp
        val sectionHeaderGap = 10.dp
        val sectionNumberWidth = 16.dp
        val sideBottomPadding = 16.dp
        val sideGroupGap = 20.dp
        val sideGroupTitlePadding = 6.dp
        val sideItemGap = 2.dp
        val sideItemHeight = 32.dp
        val sideItemPadding = 10.dp
        val sidePadding = 12.dp
        val sideWidth = 200.dp
        val stepNumberTopPadding = 24.dp
        val stepNumberWidth = 28.dp
        val tableCellEndPadding = 16.dp
        val tableCellPadding = 12.dp
        val tableCellStartPadding = 8.dp
        val tableHeaderPadding = 8.dp
        val toolbarActionsGap = 16.dp
        val toolbarHeight = 64.dp
        val toolbarTitleGap = 8.dp
        val toolbarTitleMinWidth = 240.dp
        val windowHeight = 800.dp
        val windowWidth = 1280.dp
    }

    object Type {

        private val inter = FontFamily(
            Font(resource = "font/inter_regular.ttf", weight = FontWeight.Normal),
            Font(resource = "font/inter_semibold.ttf", weight = FontWeight.SemiBold),
        )

        private val jetBrainsMono = FontFamily(Font(resource = "font/jetbrains_mono_regular.ttf"))

        private val fullLineHeight = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        )

        val body = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp)
        val brand = TextStyle(fontFamily = inter, fontSize = 15.sp, lineHeight = 20.sp)
        val heading = TextStyle(fontFamily = inter, fontSize = 15.sp, lineHeight = 20.sp)
        val mono = TextStyle(fontFamily = jetBrainsMono, fontSize = 12.sp, lineHeight = 19.sp)
        val small = TextStyle(fontFamily = inter, fontSize = 12.sp, lineHeight = 16.sp)
        val sub = TextStyle(fontFamily = inter, fontSize = 13.sp, lineHeight = 18.sp)
        val tableCell = body.copy(lineHeightStyle = fullLineHeight)
        val tableHeader = small.copy(
            lineHeight = 20.sp,
            lineHeightStyle = fullLineHeight,
        )
        val title = TextStyle(fontFamily = inter, fontSize = 17.sp, lineHeight = 22.sp)
    }
}
