package com.example.omarchy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.example.omarchy.model.IconSetPreset
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.WindowLayoutMode

@Composable
fun ControlCenterModal(
  theme: OmarchyThemeConfig,
  currentLayoutMode: WindowLayoutMode,
  isFullscreen: Boolean,
  isPersisted: Boolean = false,
  lastSavedTime: String? = null,
  persistenceStatus: String = "Room DB Ready",
  onSelectTheme: (ThemePreset) -> Unit,
  onSelectIconSet: (IconSetPreset) -> Unit = {},
  onCycleTheme: () -> Unit = {},
  onCycleIconSet: () -> Unit = {},
  onToggleLayoutMode: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onSaveWorkspace: () -> Unit = {},
  onRestoreWorkspace: () -> Unit = {},
  onClearWorkspace: () -> Unit = {},
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
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
        .border(1.5.dp, theme.accentPrimary.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
        .clickable(enabled = false) {}
        .padding(16.dp)
        .testTag("control_center_modal")
    ) {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Title Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.accentPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text("󰣇", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
              text = "OMARCHY CONTROL CENTER",
              color = theme.textPrimary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            )
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp).testTag("control_center_close_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted, modifier = Modifier.size(16.dp))
          }
        }

        // Theme Selector Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Palette, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(14.dp))
              Text(
                text = "COLOR SCHEMES (${ThemePreset.entries.size} PRESETS)",
                color = theme.accentPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(theme.surfaceVariant)
                .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
                .clickable { onCycleTheme() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("cycle_theme_button"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Next 󰒓",
                color = theme.accentPrimary,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ThemePreset.entries.forEach { preset ->
              val isSelected = theme.preset == preset
              val presetConfig = OmarchyThemeConfig.fromPreset(preset, theme.iconSet)

              Box(
                modifier = Modifier
                  .width(135.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(presetConfig.surfaceColor)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) presetConfig.accentPrimary else presetConfig.surfaceBorder,
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { onSelectTheme(preset) }
                  .padding(8.dp)
                  .testTag("theme_preset_${preset.name.lowercase()}")
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = preset.displayName,
                      color = presetConfig.textPrimary,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      maxLines = 1
                    )
                    if (isSelected) {
                      Box(
                        modifier = Modifier
                          .size(6.dp)
                          .clip(CircleShape)
                          .background(presetConfig.accentPrimary)
                      )
                    }
                  }

                  Text(
                    text = preset.authorOrOrigin,
                    color = presetConfig.textMuted,
                    fontSize = 8.sp,
                    maxLines = 1
                  )

                  // Color Swatches
                  Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(presetConfig.accentPrimary))
                    Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(presetConfig.accentSecondary))
                    Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(presetConfig.terminalGreen))
                    Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(presetConfig.terminalCyan))
                  }
                }
              }
            }
          }
        }

        // Icon Set Selector Section (Hyprland Rices)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Category, contentDescription = null, tint = theme.accentSecondary, modifier = Modifier.size(14.dp))
              Text(
                text = "HYPRLAND ICON SETS (${IconSetPreset.entries.size} PRESETS)",
                color = theme.accentSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(theme.surfaceVariant)
                .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
                .clickable { onCycleIconSet() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("cycle_icon_set_button"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Cycle 󰕰",
                color = theme.accentSecondary,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            IconSetPreset.entries.forEach { set ->
              val isSelected = theme.iconSet == set

              Box(
                modifier = Modifier
                  .width(160.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(theme.surfaceVariant)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) theme.accentSecondary else theme.surfaceBorder,
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { onSelectIconSet(set) }
                  .padding(8.dp)
                  .testTag("icon_set_preset_${set.name.lowercase()}")
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = set.displayName,
                      color = if (isSelected) theme.textPrimary else theme.textSecondary,
                      fontSize = 10.5.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isSelected) {
                      Box(
                        modifier = Modifier
                          .size(6.dp)
                          .clip(CircleShape)
                          .background(theme.accentSecondary)
                      )
                    }
                  }

                  Text(
                    text = set.subtitle,
                    color = theme.textMuted,
                    fontSize = 8.sp,
                    maxLines = 1
                  )

                  // Live Glyph Preview Badges
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    listOf(
                      set.terminalGlyph to theme.terminalGreen,
                      set.neovimGlyph to theme.accentPrimary,
                      set.obsidianGlyph to theme.accentSecondary,
                      set.btopGlyph to theme.terminalRed,
                      set.agentGlyph to theme.terminalCyan
                    ).forEach { (glyph, col) ->
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(3.dp))
                          .background(theme.surfaceColor)
                          .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(3.dp))
                          .padding(horizontal = 3.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = glyph,
                          color = col,
                          fontSize = 9.sp,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Quick Toggles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "HYPRLAND COMPOSITOR TOGGLES",
            color = theme.textMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Layout Toggle
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.surfaceVariant)
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
                .clickable { onToggleLayoutMode() }
                .padding(10.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = when (currentLayoutMode) {
                    WindowLayoutMode.SPLIT_VERTICAL -> Icons.Default.ViewColumn
                    WindowLayoutMode.SPLIT_HORIZONTAL -> Icons.Default.ViewAgenda
                    WindowLayoutMode.MASTER_STACK -> Icons.Default.GridView
                    WindowLayoutMode.TRI_ZONE_DEV -> Icons.Default.ViewQuilt
                    WindowLayoutMode.FULLSCREEN -> Icons.Default.Fullscreen
                    WindowLayoutMode.FLOATING -> Icons.Default.GridView
                  },
                  contentDescription = null,
                  tint = theme.terminalCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = currentLayoutMode.displayName,
                  color = theme.textPrimary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            // Fullscreen Toggle
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.surfaceVariant)
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
                .clickable { onToggleFullscreen() }
                .padding(10.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = theme.terminalGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = if (isFullscreen) "Exit Fullscreen" else "Fullscreen Mode",
                  color = theme.textPrimary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Workspace Persistence (Room SQLite Database)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Storage, contentDescription = null, tint = theme.terminalCyan, modifier = Modifier.size(14.dp))
              Text(
                text = "WORKSPACE PERSISTENCE (ROOM DB)",
                color = theme.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            // Status badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isPersisted) theme.terminalGreen.copy(alpha = 0.2f) else theme.accentSecondary.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = persistenceStatus,
                color = if (isPersisted) theme.terminalGreen else theme.accentSecondary,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(theme.terminalBackground)
              .border(1.dp, theme.surfaceBorder, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Database:", color = theme.textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("omarchy_workspace.db (Room SQLite)", color = theme.textPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Snapshot State:", color = theme.textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(
                  text = if (lastSavedTime != null) "Synced at $lastSavedTime" else "Auto-saving on layout shifts",
                  color = if (lastSavedTime != null) theme.terminalGreen else theme.accentSecondary,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              Text(
                text = "Recovers open windows, active workspaces, Neovim buffers & Obsidian notes across app restarts.",
                color = theme.textMuted,
                fontSize = 8.5.sp,
                lineHeight = 11.sp
              )
            }
          }

          // Persistence Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Save Snapshot Button
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.accentPrimary.copy(alpha = 0.15f))
                .border(1.dp, theme.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .clickable { onSaveWorkspace() }
                .padding(vertical = 8.dp, horizontal = 6.dp)
                .testTag("save_workspace_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(13.dp))
                Text(
                  text = "Save Snapshot",
                  color = theme.accentPrimary,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Restore Snapshot Button
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.terminalGreen.copy(alpha = 0.15f))
                .border(1.dp, theme.terminalGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .clickable { onRestoreWorkspace() }
                .padding(vertical = 8.dp, horizontal = 6.dp)
                .testTag("restore_workspace_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = theme.terminalGreen, modifier = Modifier.size(13.dp))
                Text(
                  text = "Restore Snapshot",
                  color = theme.terminalGreen,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Clear Database Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(theme.surfaceVariant)
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
                .clickable { onClearWorkspace() }
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .testTag("clear_workspace_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.DeleteOutline, contentDescription = "Clear DB", tint = theme.textMuted, modifier = Modifier.size(13.dp))
            }
          }
        }

        // System Info (Neofetch Summary)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.terminalBackground)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = theme.terminalCyan, modifier = Modifier.size(13.dp))
              Text("System Diagnostics", color = theme.terminalCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Text("OS: Omarchy Linux 4.2 (Arch base) on Android", color = theme.textPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Compositor: Hyprland (Wayland Tiling)", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Shell: Quickshell v0.4.0 (Single Unified Desktop Shell)", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Agent: Agentic Linux Copilot (agentd active)", color = theme.terminalGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Original OS & Vision: David Heinemeier Hansson (DHH)", color = theme.textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Mobile Architecture: @aegntic (100% Sovereign & Offline)", color = theme.accentPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
