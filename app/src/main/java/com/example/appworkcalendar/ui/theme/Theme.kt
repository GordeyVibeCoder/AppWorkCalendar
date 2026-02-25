package com.example.appworkcalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F7EE8),
    onPrimary = Color.White,
    background = Color(0xFFF3F7FF),
    surface = Color(0x99FFFFFF),
    onSurface = Color(0xFF1A1D24)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FB5FF),
    background = Color(0xFF0E1118),
    surface = Color(0x33353F57),
    onSurface = Color(0xFFE8EDF8)
)

@Composable
fun AppWorkCalendarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = GlassTypography,
        content = content
    )
}
