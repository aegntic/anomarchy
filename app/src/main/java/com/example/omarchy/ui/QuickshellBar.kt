package com.example.omarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * QuickshellBar:
 * A Quickshell-inspired modular status bar component for Omarchy.
 * Features:
 * - Island / pill architecture with glassmorphic styling
 * - Workspaces module with active glowing pills & window count indicators
 * - Real-time system metrics (CPU usage with micro-gauge & RAM usage with meter)
 * - Interactive telemetry popover with CPU history sparkline and memory details
 * - Active window title & application type indicator
 * - Clock / date widget and compositor layout controls
 */
@Composable
fun QuickshellBar(
  theme: OmarchyThemeConfig,
  currentWorkspace: Int,
  windows: List<WindowInstance>,
  focusedWindow: WindowInstance?,
  layoutMode: WindowLayoutMode,
  cpuPercent: Float,
  memPercent: Float,
  isHudOpen: Boolean = true,
  cpuHistory: List<Float> = emptyList(),
  onWorkspaceSelect: (Int) -> Unit,
  onToggleControlCenter: () -> Unit,
  onToggleKeybinds: () -> Unit,
  onToggleHud: () -> Unit = {},
  onOpenWalker: () -> Unit,
  onToggleLayoutMode: () -> Unit,
  onLaunchBtop: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
  val currentDate = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())

  var isMetricsFlyoutOpen by remember { mutableStateOf(false) }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .testTag("quickshell_status_bar")
  ) {
    val isNarrow = maxWidth < 600.dp
    // Top Bar Container
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .background(theme.barBackground)
        .border(
          width = 1.dp,
          color = theme.surfaceBorder.copy(alpha = 0.7f),
          shape = RoundedCornerShape(0.dp)
        )
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // -------------------------------------------------------------
      // LEFT ISLAND: Omarchy Logo & Workspace Indicators
      // -------------------------------------------------------------
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Omarchy Logo Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onToggleControlCenter() }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("omarchy_logo_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "󰣇", // Arch glyph representation
              color = theme.accentPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "OMA",
              color = theme.barText,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            )
          }
        }

        // Workspaces Module (1..5)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .testTag("quickshell_workspaces_island"),
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (ws in 1..5) {
            val isActive = ws == currentWorkspace
            val workspaceWins = windows.filter { it.workspaceId == ws }
            val winCount = workspaceWins.size

            val tabWidth by animateDpAsState(
              targetValue = if (isActive) 30.dp else 24.dp,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              label = "ws_width"
            )
            val bg by animateColorAsState(
              targetValue = if (isActive) theme.barActiveWorkspace else Color.Transparent,
              animationSpec = tween(150),
              label = "ws_bg"
            )
            val textColor by animateColorAsState(
              targetValue = if (isActive) Color.White else if (winCount > 0) theme.textPrimary else theme.textMuted,
              label = "ws_text"
            )

            Box(
              modifier = Modifier
                .width(tabWidth)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .clickable { onWorkspaceSelect(ws) }
                .testTag("workspace_tab_$ws"),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                val wsGlyph = theme.getWorkspaceGlyph(ws)
                Text(
                  text = wsGlyph,
                  color = textColor,
                  fontSize = if (wsGlyph.length > 2) 9.sp else 10.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                // Window presence indicator dots
                if (winCount > 0 && !isActive) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    repeat(winCount.coerceAtMost(3)) {
                      Box(
                        modifier = Modifier
                          .size(2.5.dp)
                          .clip(CircleShape)
                          .background(theme.accentSecondary)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Layout Toggle Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
            .clickable { onToggleLayoutMode() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("layout_mode_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (layoutMode) {
              WindowLayoutMode.SPLIT_VERTICAL -> Icons.Default.ViewColumn
              WindowLayoutMode.SPLIT_HORIZONTAL -> Icons.Default.ViewAgenda
              WindowLayoutMode.MASTER_STACK -> Icons.Default.GridView
              WindowLayoutMode.TRI_ZONE_DEV -> Icons.Default.ViewQuilt
              WindowLayoutMode.FULLSCREEN -> Icons.Default.Fullscreen
              WindowLayoutMode.FLOATING -> Icons.Default.GridView
            },
            contentDescription = "Layout Mode: ${layoutMode.displayName}",
            tint = theme.accentSecondary,
            modifier = Modifier.size(13.dp)
          )
        }
      }

      // -------------------------------------------------------------
      // CENTER ISLAND: Active Window / Task Indicator
      // -------------------------------------------------------------
      Row(
        modifier = Modifier
          .weight(1f, fill = false)
          .padding(horizontal = 8.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(theme.surfaceColor.copy(alpha = 0.6f))
          .border(0.5.dp, theme.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("active_window_island"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        val glyph = if (focusedWindow != null) theme.getAppGlyph(focusedWindow.appType) else theme.iconSet.distroGlyph
        Text(
          text = glyph,
          color = if (focusedWindow != null) theme.accentPrimary else theme.textMuted,
          fontSize = 11.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = focusedWindow?.title ?: "Omarchy Desktop",
          color = theme.barText,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // -------------------------------------------------------------
      // RIGHT ISLAND: System Metrics + Clock + Tray Tools
      // -------------------------------------------------------------
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        // SYSTEM METRICS PILL (CPU & MEMORY with micro progress meters)
        QuickshellMetricsPill(
          cpuPercent = cpuPercent,
          memPercent = memPercent,
          theme = theme,
          isFlyoutOpen = isMetricsFlyoutOpen,
          onClick = { isMetricsFlyoutOpen = !isMetricsFlyoutOpen }
        )

        // TIME & DATE PILL
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("quickshell_clock_pill"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = theme.textMuted,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = currentTime,
              color = theme.textPrimary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // TRAY TOOLS: Walker, HUD, Keybinds, Control Center
        Row(
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Walker Search Trigger
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(theme.surfaceColor)
              .border(1.dp, theme.surfaceBorder, RoundedCornerShape(5.dp))
              .clickable { onOpenWalker() }
              .testTag("walker_launcher_icon"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Walker Launcher",
              tint = theme.accentPrimary,
              modifier = Modifier.size(13.dp)
            )
          }

          if (!isNarrow) {
            // HUD Toggle Icon
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (isHudOpen) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceColor)
                .border(
                  1.dp,
                  if (isHudOpen) theme.accentPrimary else theme.surfaceBorder,
                  RoundedCornerShape(5.dp)
                )
                .clickable { onToggleHud() }
                .testTag("hud_toggle_icon"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Shortcuts HUD",
                tint = if (isHudOpen) theme.accentPrimary else theme.textSecondary,
                modifier = Modifier.size(13.dp)
              )
            }

            // Keybinds help icon
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(theme.surfaceColor)
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(5.dp))
                .clickable { onToggleKeybinds() }
                .testTag("keybinds_help_icon"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Keybindings",
                tint = theme.textSecondary,
                modifier = Modifier.size(12.dp)
              )
            }
          }

          // Control Center Trigger
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(theme.surfaceColor)
              .border(1.dp, theme.surfaceBorder, RoundedCornerShape(5.dp))
              .clickable { onToggleControlCenter() }
              .testTag("control_center_icon"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Control Center",
              tint = theme.textPrimary,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }

    // -------------------------------------------------------------
    // QUICKSHELL METRICS FLYOUT (Dropdown Telemetry Popover)
    // -------------------------------------------------------------
    AnimatedVisibility(
      visible = isMetricsFlyoutOpen,
      enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { -it / 2 },
      exit = fadeOut(tween(100)) + slideOutVertically(tween(100)) { -it / 2 },
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 48.dp, end = 8.dp)
    ) {
      QuickshellTelemetryFlyout(
        cpuPercent = cpuPercent,
        memPercent = memPercent,
        cpuHistory = cpuHistory,
        theme = theme,
        onClose = { isMetricsFlyoutOpen = false },
        onLaunchBtop = {
          isMetricsFlyoutOpen = false
          onLaunchBtop()
        }
      )
    }
  }
}

/**
 * Quickshell Metrics Pill with graphical micro progress bars for CPU and RAM.
 */
@Composable
private fun QuickshellMetricsPill(
  cpuPercent: Float,
  memPercent: Float,
  theme: OmarchyThemeConfig,
  isFlyoutOpen: Boolean,
  onClick: () -> Unit
) {
  val cpuColor = when {
    cpuPercent > 80f -> theme.terminalRed
    cpuPercent > 50f -> theme.terminalYellow
    else -> theme.terminalGreen
  }

  val ramColor = when {
    memPercent > 85f -> theme.terminalRed
    memPercent > 65f -> theme.terminalYellow
    else -> theme.terminalCyan
  }

  val borderTint by animateColorAsState(
    if (isFlyoutOpen) theme.accentPrimary else theme.surfaceBorder,
    label = "metrics_border"
  )

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(theme.surfaceColor)
      .border(1.dp, borderTint, RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 7.dp, vertical = 3.dp)
      .testTag("system_metrics_pill"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // CPU Metric Column
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Speed,
        contentDescription = null,
        tint = cpuColor,
        modifier = Modifier.size(11.dp)
      )
      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
          text = "CPU ${cpuPercent.toInt()}%",
          color = cpuColor,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        // Micro CPU meter bar
        Box(
          modifier = Modifier
            .width(26.dp)
            .height(2.5.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(theme.surfaceBorder)
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(fraction = (cpuPercent / 100f).coerceIn(0.05f, 1f))
              .background(cpuColor)
          )
        }
      }
    }

    // Divider
    Box(
      modifier = Modifier
        .width(1.dp)
        .height(14.dp)
        .background(theme.surfaceBorder)
    )

    // Memory Metric Column
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Memory,
        contentDescription = null,
        tint = ramColor,
        modifier = Modifier.size(11.dp)
      )
      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
          text = "RAM ${memPercent.toInt()}%",
          color = ramColor,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        // Micro RAM meter bar
        Box(
          modifier = Modifier
            .width(26.dp)
            .height(2.5.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(theme.surfaceBorder)
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(fraction = (memPercent / 100f).coerceIn(0.05f, 1f))
              .background(ramColor)
          )
        }
      }
    }
  }
}

/**
 * Quickshell Telemetry Flyout:
 * Detailed system monitor dropdown displaying CPU sparkline history, RAM breakdown,
 * system info, and quick launch into btop.
 */
@Composable
private fun QuickshellTelemetryFlyout(
  cpuPercent: Float,
  memPercent: Float,
  cpuHistory: List<Float>,
  theme: OmarchyThemeConfig,
  onClose: () -> Unit,
  onLaunchBtop: () -> Unit
) {
  val usedGb = (16.0f * (memPercent / 100f))
  val freeGb = 16.0f - usedGb

  Column(
    modifier = Modifier
      .width(260.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(theme.surfaceColor.copy(alpha = 0.98f))
      .border(1.dp, theme.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
      .padding(10.dp)
      .testTag("quickshell_telemetry_flyout"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Flyout Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MonitorHeart,
          contentDescription = null,
          tint = theme.accentPrimary,
          modifier = Modifier.size(13.dp)
        )
        Text(
          text = "QUICKSHELL TELEMETRY",
          color = theme.accentPrimary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
      IconButton(
        onClick = onClose,
        modifier = Modifier.size(18.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close Telemetry Flyout",
          tint = theme.textSecondary,
          modifier = Modifier.size(12.dp)
        )
      }
    }

    // CPU Section with Sparkline Graph
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "CPU LOAD",
          color = theme.textSecondary,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "${cpuPercent.toInt()}% @ 4.20 GHz",
          color = theme.terminalGreen,
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      // Sparkline Graph
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(36.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(theme.terminalBackground)
          .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp, vertical = 2.dp)
      ) {
        val history = if (cpuHistory.isNotEmpty()) cpuHistory else listOf(12f, 18f, 25f, 20f, 32f, 15f, cpuPercent)
        Canvas(modifier = Modifier.fillMaxSize()) {
          val count = history.size
          if (count > 1) {
            val barWidth = size.width / count
            for (i in 0 until count) {
              val h = (history[i] / 100f) * size.height
              val color = when {
                history[i] > 80f -> Color(0xFFF7768E)
                history[i] > 50f -> Color(0xFFE0AF68)
                else -> Color(0xFF73DACA)
              }
              drawRoundRect(
                color = color,
                topLeft = Offset(i * barWidth + 1.dp.toPx(), size.height - h),
                size = Size(barWidth - 2.dp.toPx(), h),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
              )
            }
          }
        }
      }
    }

    // RAM Section with Usage Bar
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "MEMORY (RAM)",
          color = theme.textSecondary,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = String.format(Locale.US, "%.1f / 16.0 GB", usedGb),
          color = theme.terminalCyan,
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      // Progress Bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(theme.surfaceVariant)
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = (memPercent / 100f).coerceIn(0.05f, 1f))
            .background(
              Brush.horizontalGradient(
                listOf(theme.terminalCyan, theme.accentSecondary)
              )
            )
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = String.format(Locale.US, "Free: %.1f GB", freeGb),
          color = theme.textMuted,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Swap: 0.2 / 8.0 GB",
          color = theme.textMuted,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // System Details
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .background(theme.surfaceVariant)
        .padding(6.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Compositor", color = theme.textMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text("Hyprland 0.45.0", color = theme.textPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Kernel", color = theme.textMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text("6.12.9-omarchy-zen", color = theme.textPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Uptime", color = theme.textMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text("4 hrs 12 mins", color = theme.terminalGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }
    }

    // Quick Action: Launch BTOP Monitor
    Button(
      onClick = onLaunchBtop,
      colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary),
      shape = RoundedCornerShape(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(28.dp)
        .testTag("launch_btop_from_quickshell")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
        Text(
          text = "OPEN BTOP MONITOR",
          color = Color.Black,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
