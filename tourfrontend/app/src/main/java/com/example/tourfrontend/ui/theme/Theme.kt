package com.example.tourfrontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = LightBlue80,
    onSecondaryContainer = Blue40,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFCCCCCC)
)

private val DarkColors = darkColorScheme(
    primary = SecondaryBlue,
    onPrimary = Color.Black,
    primaryContainer = Blue40,
    onPrimaryContainer = Color.White,
    secondary = LightBlue40,
    onSecondary = Color.Black,
    secondaryContainer = BlueGrey40,
    onSecondaryContainer = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF616161)
)

@Composable
fun TourfrontendTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}