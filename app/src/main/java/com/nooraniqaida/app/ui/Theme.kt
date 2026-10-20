package com.nooraniqaida.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF1B6B4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7EFCF),
    onPrimaryContainer = Color(0xFF002113),
    secondary = Color(0xFF8A6D1E),
    surfaceVariant = Color(0xFFE6EFE9),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8CD7B0),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF00522F),
    onPrimaryContainer = Color(0xFFB7EFCF),
    secondary = Color(0xFFE5C46A),
    surfaceVariant = Color(0xFF2A332E),
)

@Composable
fun QaidaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
