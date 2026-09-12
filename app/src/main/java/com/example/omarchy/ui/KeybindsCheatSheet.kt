package com.example.omarchy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.omarchy.model.OmarchyThemeConfig

data class KeybindItem(
  val combo: String,
  val description: String,
  val category: String
)

@Composable
fun KeybindsCheatSheet(
  theme: OmarchyThemeConfig,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val keybinds = listOf(
    KeybindItem("Super + Return", "Launch Alacritty Terminal", "Window & Apps"),
    KeybindItem("Super + Space", "Open Walker Application Launcher", "Window & Apps"),
    KeybindItem("Super + Q", "Close (Kill) focused window", "Window Management"),
    KeybindItem("Super + F", "Toggle Fullscreen mode", "Window Management"),
    KeybindItem("Super + V", "Toggle Vertical / Horizontal split", "Window Management"),
    KeybindItem("Super + 1..5", "Switch to Workspace 1 through 5", "Workspaces"),
    KeybindItem("Super + E", "Launch Neovim editor", "Development"),
    KeybindItem("Ctrl + P", "Telescope fuzzy file finder (in Nvim)", "Editor"),
    KeybindItem("Super + A", "Open Agentic Linux Copilot", "AI"),
    KeybindItem("Super + O", "Open Obsidian notes", "Productivity"),
    KeybindItem("Super + B", "Open Chromium browser", "Internet"),
    KeybindItem("Super + M", "Open Command Macro Studio", "Macros & Automation"),
    KeybindItem("Super + F1", "Macro: Rails Scaffold & Dev Boot", "Macros & Automation"),
    KeybindItem("Super + F2", "Macro: Git Sync & Commit", "Macros & Automation"),
    KeybindItem("Super + F3", "Macro: Tri-Zone Split Layout", "Macros & Automation"),
    KeybindItem("Super + F4", "Macro: Obsidian Quick Note Setup", "Macros & Automation"),
    KeybindItem("Super + Shift + M", "Open btop++ monitor", "System")
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.65f))
      .clickable { onClose() }
      .padding(horizontal = 16.dp, vertical = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(theme.surfaceColor)
        .border(1.5.dp, theme.accentSecondary, RoundedCornerShape(12.dp))
        .clickable(enabled = false) {}
        .padding(16.dp)
        .testTag("keybinds_cheatsheet_modal")
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Keyboard, contentDescription = null, tint = theme.accentSecondary, modifier = Modifier.size(18.dp))
            Text(
              text = "HYPRLAND KEYBINDINGS",
              color = theme.textPrimary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp).testTag("keybinds_close_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted, modifier = Modifier.size(16.dp))
          }
        }

        Text(
          text = "Omarchy is keyboard-centric. Tap dock hotkeys or use a physical keyboard:",
          color = theme.textSecondary,
          fontSize = 11.sp
        )

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(keybinds) { bind ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(theme.surfaceVariant)
                .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(theme.terminalBackground)
                  .border(1.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = bind.combo,
                  color = theme.accentPrimary,
                  fontSize = 10.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = bind.description,
                  color = theme.textPrimary,
                  fontSize = 11.sp
                )
                Text(
                  text = bind.category,
                  color = theme.textMuted,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Lineage & Sovereignty Footer
        Text(
          text = "Omarchy Mobile • Original OS & Vision by David Heinemeier Hansson (DHH) • Mobile Architecture by @aegntic • 100% Sovereign & Offline",
          color = theme.textMuted,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}
