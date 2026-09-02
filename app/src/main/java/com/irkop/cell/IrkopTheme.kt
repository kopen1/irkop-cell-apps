package com.irkop.cell

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IrkopPrimary = Color(0xFF6847B7)
private val IrkopSurface = Color(0xFFFBF9FF)
private val IrkopSurfaceContainer = Color(0xFFF1ECF8)

private val LightColors = lightColorScheme(
    primary = IrkopPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF25005A),
    secondary = Color(0xFF625B71),
    surface = IrkopSurface,
    surfaceContainer = IrkopSurfaceContainer,
    background = IrkopSurface,
    error = Color(0xFFB3261E),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun IrkopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
