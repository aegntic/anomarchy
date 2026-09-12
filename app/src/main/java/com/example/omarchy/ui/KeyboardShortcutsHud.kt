package com.example.omarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode

data class HudShortcut(
  val keys: String,
  val action: String,
  val onClick: (() -> Unit)? = null
)

@Composable
fun KeyboardShortcutsHud(
  theme: OmarchyThemeConfig,
  focusedWindow: WindowInstance?,
  isWalkerOpen: Boolean,
  isControlCenterOpen: Boolean,
  isKeybindsOpen: Boolean,
  isCollapsed: Boolean,
  layoutMode: WindowLayoutMode,
  isFullscreen: Boolean,
  neovimMode: String,
  onToggleCollapse: () -> Unit,
  onClose: () -> Unit,
  onLaunchTerminal: () -> Unit,
  onOpenWalker: () -> Unit,
  onCloseWindow: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onToggleLayout: () -> Unit,
  onExecuteCmd: (String) -> Unit,
  onSetNeovimMode: (String) -> Unit,
  onToggleObsidianPreview: () -> Unit,
  onCreateNote: () -> Unit,
  onCycleTheme: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Determine screen context and available shortcuts
  val (screenTitle, contextShortcuts) = when {
    isWalkerOpen -> {
      "WALKER LAUNCHER" to listOf(
        HudShortcut("Type", "Fuzzy search apps & tools"),
        HudShortcut("12 * 8", "Instant arithmetic evaluation"),
        HudShortcut("Click / Tap", "Launch selected app"),
        HudShortcut("Esc / Tap Out", "Dismiss Walker", onClose)
      )
    }
    isControlCenterOpen -> {
      "CONTROL CENTER" to listOf(
        HudShortcut("Presets", "Tap preset to switch theme", onCycleTheme),
        HudShortcut("L", "Cycle layout: ${layoutMode.displayName}", onToggleLayout),
        HudShortcut("F", if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen", onToggleFullscreen),
        HudShortcut("Esc", "Close Control Center", onClose)
      )
    }
    isKeybindsOpen -> {
      "KEYBINDS HELP" to listOf(
        HudShortcut("Scroll", "Browse all Hyprland bindings"),
        HudShortcut("Esc", "Close Cheat Sheet", onClose)
      )
    }
    focusedWindow != null -> {
      when (focusedWindow.appType) {
        AppType.TERMINAL -> {
          "ALACRITTY TERMINAL" to listOf(
            HudShortcut("Enter", "Execute typed shell command"),
            HudShortcut("omafetch", "Display Arch & Omarchy info", { onExecuteCmd("omafetch") }),
            HudShortcut("theme", "Cycle desktop theme preset", { onExecuteCmd("theme") }),
            HudShortcut("rails", "Scaffold blog application", { onExecuteCmd("rails new blog") }),
            HudShortcut("clear", "Wipe terminal buffer", { onExecuteCmd("clear") }),
            HudShortcut("Super + Q", "Close terminal window", onCloseWindow)
          )
        }
        AppType.NEOVIM -> {
          "NEOVIM [$neovimMode]" to listOf(
            HudShortcut("Ctrl + P", "Telescope fuzzy file finder"),
            HudShortcut(if (neovimMode == "NORMAL") "i" else "Esc", if (neovimMode == "NORMAL") "Enter INSERT mode" else "Switch to NORMAL mode", {
              onSetNeovimMode(if (neovimMode == "NORMAL") "INSERT" else "NORMAL")
            }),
            HudShortcut("1 .. 3", "Select buffer tab"),
            HudShortcut(":w", "Save current buffer", { onSetNeovimMode("NORMAL") }),
            HudShortcut("Super + F", "Fullscreen editor toggle", onToggleFullscreen),
            HudShortcut("Super + Q", "Quit Neovim window", onCloseWindow)
          )
        }
        AppType.OBSIDIAN -> {
          "OBSIDIAN NOTES" to listOf(
            HudShortcut("Ctrl + E", "Toggle Edit / Markdown preview", onToggleObsidianPreview),
            HudShortcut("Ctrl + N", "Create new blank note", onCreateNote),
            HudShortcut("Menu 󰍜", "Toggle vault sidebar explorer"),
            HudShortcut("Super + F", "Fullscreen view toggle", onToggleFullscreen),
            HudShortcut("Super + Q", "Close Obsidian window", onCloseWindow)
          )
        }
        AppType.BTOP -> {
          "BTOP++ MONITOR" to listOf(
            HudShortcut("KILL", "Terminate running PID process"),
            HudShortcut("Super + V", "Toggle Hyprland split mode", onToggleLayout),
            HudShortcut("Super + F", "Fullscreen process view", onToggleFullscreen),
            HudShortcut("Super + Q", "Close monitor window", onCloseWindow)
          )
        }
        AppType.AGENT -> {
          "AGENTIC COPILOT" to listOf(
            HudShortcut("Enter", "Send instructions to agent"),
            HudShortcut("Pills", "One-tap prompt suggestions"),
            HudShortcut("Super + F", "Fullscreen agent console", onToggleFullscreen),
            HudShortcut("Super + Q", "Close agent window", onCloseWindow)
          )
        }
        AppType.BROWSER -> {
          "CHROMIUM BROWSER" to listOf(
            HudShortcut("Enter", "Navigate to entered URL"),
            HudShortcut("Bookmarks", "Quick launch Omarchy / Rails"),
            HudShortcut("Super + F", "Fullscreen web view", onToggleFullscreen),
            HudShortcut("Super + Q", "Close browser window", onCloseWindow)
          )
        }
      }
    }
    layoutMode == WindowLayoutMode.TRI_ZONE_DEV && focusedWindow == null -> {
      "DEV COCKPIT (TRI-ZONE)" to listOf(
        HudShortcut("Presets", "Cockpit | 3-Cols | Sidebars | Grid"),
        HudShortcut("Ratio", "Adjust 50:25:25 / 1:1:1 weights"),
        HudShortcut("Zones", "Toggle Terminal / Editor / AI"),
        HudShortcut("Swap", "Cycle and rotate split positions"),
        HudShortcut("Super + V", "Cycle layout: ${layoutMode.displayName}", onToggleLayout),
        HudShortcut("Super + H", "Toggle this HUD", onClose)
      )
    }
    else -> {
      "HYPRLAND DESKTOP" to listOf(
        HudShortcut("Super + Enter", "Launch Terminal", onLaunchTerminal),
        HudShortcut("Super + Space", "Open Walker launcher", onOpenWalker),
        HudShortcut("Super + 1..5", "Switch workspace tabs"),
        HudShortcut("Super + V", "Toggle Split layout: ${layoutMode.displayName}", onToggleLayout),
        HudShortcut("Super + F", "Toggle Fullscreen", onToggleFullscreen),
        HudShortcut("Super + H", "Toggle this Shortcuts HUD", onClose)
      )
    }
  }

  // Pulsing animation for the active HUD indicator
  val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  AnimatedVisibility(
    visible = true,
    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    modifier = modifier
  ) {
    if (isCollapsed) {
      // Collapsed Pill View
      Box(
        modifier = Modifier
          .shadow(8.dp, RoundedCornerShape(20.dp))
          .clip(RoundedCornerShape(20.dp))
          .background(theme.surfaceColor.copy(alpha = 0.94f))
          .border(1.dp, theme.accentPrimary.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
          .clickable { onToggleCollapse() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("hud_collapsed_pill"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(theme.terminalGreen)
              .alpha(pulseAlpha)
          )
          Icon(
            imageVector = Icons.Default.Keyboard,
            contentDescription = "Keys HUD",
            tint = theme.accentPrimary,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = "HUD: ${contextShortcuts.size} KEYS",
            color = theme.textPrimary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Expand HUD",
            tint = theme.textSecondary,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    } else {
      // Expanded HUD Window
      Box(
        modifier = Modifier
          .widthIn(min = 250.dp, max = 285.dp)
          .shadow(12.dp, RoundedCornerShape(10.dp))
          .clip(RoundedCornerShape(10.dp))
          .background(theme.surfaceColor.copy(alpha = 0.94f))
          .border(1.dp, theme.accentPrimary.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
          .padding(10.dp)
          .animateContentSize(animationSpec = spring())
          .testTag("hud_expanded_card")
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // HUD Top Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(theme.terminalGreen)
                  .alpha(pulseAlpha)
              )
              Text(
                text = "HUD // $screenTitle",
                color = theme.accentPrimary,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Window controls: Collapse & Dismiss
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(theme.surfaceVariant)
                  .clickable { onToggleCollapse() }
                  .testTag("hud_collapse_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Collapse HUD",
                  tint = theme.textSecondary,
                  modifier = Modifier.size(13.dp)
                )
              }

              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(theme.terminalRed.copy(alpha = 0.2f))
                  .clickable { onClose() }
                  .testTag("hud_close_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close HUD",
                  tint = theme.terminalRed,
                  modifier = Modifier.size(11.dp)
                )
              }
            }
          }

          // Subtle divider line
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(0.5.dp)
              .background(theme.surfaceBorder)
          )

          // List of Contextual Keyboard Shortcuts
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 190.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(contextShortcuts) { shortcut ->
              val isInteractive = shortcut.onClick != null

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(
                    if (isInteractive) theme.surfaceVariant.copy(alpha = 0.7f)
                    else theme.surfaceVariant.copy(alpha = 0.35f)
                  )
                  .then(
                    if (isInteractive) {
                      Modifier.clickable { shortcut.onClick?.invoke() }
                    } else Modifier
                  )
                  .padding(horizontal = 6.dp, vertical = 3.5.dp)
                  .testTag("hud_key_${shortcut.keys.replace(" ", "_")}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Key Badge
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(theme.terminalBackground)
                    .border(
                      0.75.dp,
                      if (isInteractive) theme.accentPrimary.copy(alpha = 0.6f)
                      else theme.surfaceBorder,
                      RoundedCornerShape(3.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = shortcut.keys,
                    color = if (isInteractive) theme.accentPrimary else theme.textSecondary,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action description
                Text(
                  text = shortcut.action,
                  color = theme.textPrimary,
                  fontSize = 10.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                  fontFamily = FontFamily.SansSerif
                )
              }
            }
          }

          // Footer Tip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Tap any key to trigger",
              color = theme.terminalGreen,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Super + H to toggle",
              color = theme.textMuted,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}
