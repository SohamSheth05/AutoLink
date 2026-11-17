package com.autolink.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Status colours, chosen for a dark in-car display. */
object AutoColors {
    val Live = Color(0xFF4ADE80)
    val Stale = Color(0xFFFBBF24)
    val Unavailable = Color(0xFFF87171)
    val Simulated = Color(0xFF60A5FA)
}

/** Automotive sizing: AAOS guidance asks for large touch targets and glanceable text. */
object Dimens {
    val touchTarget = 76.dp
    val gutter = 24.dp
    val cardPadding = 24.dp
}

private val colors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    background = Color(0xFF0B0F14),
    surface = Color(0xFF151B23),
    surfaceVariant = Color(0xFF1F2731),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF9AA7B4),
)

private val typography = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 22.sp),
    labelLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium),
)

/** Cars are dark-first; there is no light theme in v1. */
@Composable
fun AutoLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
