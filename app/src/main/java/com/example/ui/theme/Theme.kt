package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ReelNagDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = DeepViolet,
    onPrimaryContainer = LavenderTint,
    secondary = SoftLavender,
    onSecondary = CharcoalBackground,
    secondaryContainer = Color(0xFF2D2346),
    onSecondaryContainer = LavenderTint,
    tertiary = PeriwinkleAccent,
    onTertiary = CharcoalBackground,
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = ElevatedSlate,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = DangerRose,
    onError = Color.White,
    errorContainer = DangerRoseSurface,
    onErrorContainer = Color(0xFFFFD9DF)
)

val ReelNagShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ReelNagTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReelNagDarkColorScheme,
        typography = Typography,
        shapes = ReelNagShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ReelNagTheme(content = content)
}
