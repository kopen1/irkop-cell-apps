package com.irkop.cell

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IrkopPrimary = Color(0xFF6C5DD3)
private val IrkopPrimaryDark = Color(0xFF9B8CFF)

private val LightColors = lightColorScheme(
    primary = IrkopPrimary, onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E2FF), onPrimaryContainer = Color(0xFF20145E),
    secondary = Color(0xFF655F7B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E2FF), onSecondaryContainer = Color(0xFF201A35),
    background = Color.White, surface = Color.White,
    surfaceContainer = Color(0xFFF5F2FA), surfaceVariant = Color(0xFFE8E3EF),
    onSurface = Color(0xFF1B1920), onSurfaceVariant = Color(0xFF66616D),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = IrkopPrimaryDark, onPrimary = Color(0xFF241A5D),
    primaryContainer = Color(0xFF4B3F9B), onPrimaryContainer = Color(0xFFF0ECFF),
    secondary = Color(0xFFCBC2E4), onSecondary = Color(0xFF302A3D),
    secondaryContainer = Color(0xFF4A4357), onSecondaryContainer = Color(0xFFEAE2FF),
    background = Color(0xFF14121F), surface = Color(0xFF1E1B2E),
    surfaceContainer = Color(0xFF252137), surfaceVariant = Color(0xFF454052),
    onSurface = Color(0xFFF3EFF8), onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFFFB4AB),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(22.dp),
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
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
