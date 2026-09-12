package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OmarchyColorScheme = darkColorScheme(
  primary = Color(0xFF7AA2F7),
  onPrimary = Color(0xFF0F172A),
  secondary = Color(0xFFBB9AF7),
  onSecondary = Color(0xFF0F172A),
  tertiary = Color(0xFF7DCFFF),
  background = Color(0xFF16161E),
  onBackground = Color(0xFFC0CAF5),
  surface = Color(0xFF1F2335),
  onSurface = Color(0xFFC0CAF5),
  surfaceVariant = Color(0xFF24283B),
  onSurfaceVariant = Color(0xFF9AA5CE)
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = OmarchyColorScheme,
    typography = Typography,
    content = content
  )
}

