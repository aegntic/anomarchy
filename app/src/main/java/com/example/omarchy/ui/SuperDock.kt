package com.example.omarchy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig

@Composable
fun SuperDock(
  theme: OmarchyThemeConfig,
  onSuperKey: () -> Unit,
  onTerminalKey: () -> Unit,
  onKillKey: () -> Unit,
  onFullscreenKey: () -> Unit,
  onSplitKey: () -> Unit,
  onThemeKey: () -> Unit,
  onHudKey: () -> Unit = {},
  isHudOpen: Boolean = true,
  onMacroKey: () -> Unit = {},
  isRecordingMacro: Boolean = false,
  onLaunchApp: (AppType) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(theme.barBackground)
      .border(1.dp, theme.surfaceBorder.copy(alpha = 0.6f))
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .navigationBarsPadding()
      .horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // SUPER key
    KeyButton(
      label = "SUPER",
      accentColor = theme.accentPrimary,
      theme = theme,
      testTag = "super_key_button",
      onClick = onSuperKey
    )

    // ENTER -> terminal
    KeyButton(
      label = "ENTER",
      icon = Icons.Default.Terminal,
      accentColor = theme.terminalGreen,
      theme = theme,
      testTag = "enter_terminal_button",
      onClick = onTerminalKey
    )

    // Q -> kill window
    KeyButton(
      label = "Q (Kill)",
      icon = Icons.Default.Close,
      accentColor = theme.terminalRed,
      theme = theme,
      testTag = "kill_window_button",
      onClick = onKillKey
    )

    // F -> Fullscreen
    KeyButton(
      label = "F (Full)",
      icon = Icons.Default.Fullscreen,
      accentColor = theme.accentSecondary,
      theme = theme,
      testTag = "fullscreen_window_button",
      onClick = onFullscreenKey
    )

    // V -> Split
    KeyButton(
      label = "V (Split)",
      icon = Icons.Default.ViewAgenda,
      accentColor = theme.terminalCyan,
      theme = theme,
      testTag = "split_window_button",
      onClick = onSplitKey
    )

    // Quick App Shortcuts
    AppShortcutButton(app = AppType.NEOVIM, theme = theme, onClick = { onLaunchApp(AppType.NEOVIM) })
    AppShortcutButton(app = AppType.OBSIDIAN, theme = theme, onClick = { onLaunchApp(AppType.OBSIDIAN) })
    AppShortcutButton(app = AppType.BTOP, theme = theme, onClick = { onLaunchApp(AppType.BTOP) })
    AppShortcutButton(app = AppType.AGENT, theme = theme, onClick = { onLaunchApp(AppType.AGENT) })

    // Theme Switch Shortcut
    KeyButton(
      label = "THEME",
      icon = Icons.Default.Palette,
      accentColor = theme.accentSecondary,
      theme = theme,
      testTag = "dock_theme_button",
      onClick = onThemeKey
    )

    // Shortcuts HUD Key
    KeyButton(
      label = if (isHudOpen) "HUD ON" else "HUD",
      icon = Icons.Default.Keyboard,
      accentColor = if (isHudOpen) theme.accentPrimary else theme.textSecondary,
      theme = theme,
      testTag = "dock_hud_button",
      onClick = onHudKey
    )

    // Command Macro Studio Key
    KeyButton(
      label = if (isRecordingMacro) "REC ●" else "MACROS",
      icon = Icons.Default.Keyboard,
      accentColor = if (isRecordingMacro) theme.terminalRed else theme.accentPrimary,
      theme = theme,
      testTag = "dock_macro_button",
      onClick = onMacroKey
    )
  }
}

@Composable
private fun KeyButton(
  label: String,
  accentColor: Color,
  theme: OmarchyThemeConfig,
  testTag: String,
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
  Box(
    modifier = Modifier
      .height(34.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(theme.surfaceColor)
      .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(13.dp)
        )
      }
      Text(
        text = label,
        color = theme.textPrimary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
private fun AppShortcutButton(
  app: AppType,
  theme: OmarchyThemeConfig,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .height(34.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(theme.surfaceColor)
      .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 7.dp)
      .testTag("dock_app_${app.binaryName}"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = app.icon,
        contentDescription = app.displayName,
        tint = theme.accentPrimary,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = app.binaryName,
        color = theme.textSecondary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
