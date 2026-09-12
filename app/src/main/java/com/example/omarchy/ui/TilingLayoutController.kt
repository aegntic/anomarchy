package com.example.omarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.apps.AgenticConsoleApp
import com.example.omarchy.apps.NeovimApp
import com.example.omarchy.apps.TerminalApp
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.SplitRatioPreset
import com.example.omarchy.model.TilingControllerState
import com.example.omarchy.model.TilingSplitPreset
import com.example.omarchy.model.TilingZoneType
import com.example.omarchy.state.AgentMessage
import com.example.omarchy.state.NeovimBuffer
import com.example.omarchy.state.TerminalLine

/**
 * Interactive controller toolbar for the tiling window manager.
 * Allows switching split presets, toggling individual zones, adjusting ratios,
 * and swapping positions for Terminal, Editor, and Assistant components.
 */
@Composable
fun TilingLayoutControllerBar(
  state: TilingControllerState,
  theme: OmarchyThemeConfig,
  onSelectPreset: (TilingSplitPreset) -> Unit,
  onSelectRatio: (SplitRatioPreset) -> Unit,
  onToggleZone: (TilingZoneType) -> Unit,
  onSwapZones: () -> Unit,
  onSpawnDevTriad: () -> Unit,
  onToggleExpanded: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag("tiling_layout_controller_bar")
  ) {
    if (!state.isControllerExpanded) {
      // Compact collapsed pill
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(theme.surfaceColor.copy(alpha = 0.95f))
          .border(1.dp, theme.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .clickable { onToggleExpanded() }
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .align(Alignment.CenterStart)
          .testTag("tiling_controller_collapsed_pill"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = state.preset.icon,
          contentDescription = null,
          tint = theme.accentPrimary,
          modifier = Modifier.size(13.dp)
        )
        Text(
          text = "LAYOUT: ${state.preset.shortCode.uppercase()} (${state.activeZoneCount}/3 ZONES)",
          color = theme.textPrimary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "Expand Controller",
          tint = theme.textSecondary,
          modifier = Modifier.size(13.dp)
        )
      }
    } else {
      // Expanded Controller Panel
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(theme.surfaceColor.copy(alpha = 0.96f))
          .border(1.dp, theme.surfaceBorder, RoundedCornerShape(8.dp))
          .padding(6.dp)
          .testTag("tiling_controller_expanded_panel"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        // Row 1: Header + Preset Selector + Collapse Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ViewQuilt,
              contentDescription = null,
              tint = theme.accentPrimary,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = "TILING CONTROLLER",
              color = theme.accentPrimary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          // Preset Capsules
          Row(
            modifier = Modifier
              .weight(1f, fill = false)
              .padding(horizontal = 6.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TilingSplitPreset.entries.forEach { preset ->
              val isSelected = state.preset == preset
              val bg = if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceVariant
              val border = if (isSelected) theme.accentPrimary else theme.surfaceBorder
              val textCol = if (isSelected) theme.accentPrimary else theme.textSecondary

              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(bg)
                  .border(0.75.dp, border, RoundedCornerShape(4.dp))
                  .clickable { onSelectPreset(preset) }
                  .padding(horizontal = 6.dp, vertical = 2.5.dp)
                  .testTag("preset_button_${preset.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Icon(
                  imageVector = preset.icon,
                  contentDescription = null,
                  tint = textCol,
                  modifier = Modifier.size(11.dp)
                )
                Text(
                  text = preset.shortCode,
                  color = textCol,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          // Collapse button
          IconButton(
            onClick = onToggleExpanded,
            modifier = Modifier.size(20.dp).testTag("tiling_controller_collapse_btn")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowUp,
              contentDescription = "Collapse Controller",
              tint = theme.textSecondary,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        // Row 2: Zone Toggles + Ratio Presets + Actions
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Zone visibility capsules
          Text(
            text = "ZONES:",
            color = theme.textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )

          ZoneToggleCapsule(
            zone = TilingZoneType.TERMINAL,
            isVisible = state.isZoneVisible(TilingZoneType.TERMINAL),
            theme = theme,
            onClick = { onToggleZone(TilingZoneType.TERMINAL) }
          )

          ZoneToggleCapsule(
            zone = TilingZoneType.EDITOR,
            isVisible = state.isZoneVisible(TilingZoneType.EDITOR),
            theme = theme,
            onClick = { onToggleZone(TilingZoneType.EDITOR) }
          )

          ZoneToggleCapsule(
            zone = TilingZoneType.ASSISTANT,
            isVisible = state.isZoneVisible(TilingZoneType.ASSISTANT),
            theme = theme,
            onClick = { onToggleZone(TilingZoneType.ASSISTANT) }
          )

          // Divider dot
          Box(
            modifier = Modifier
              .size(3.dp)
              .clip(CircleShape)
              .background(theme.surfaceBorder)
          )

          // Ratio Presets
          Text(
            text = "RATIO:",
            color = theme.textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )

          SplitRatioPreset.entries.forEach { ratio ->
            val isSelected = state.ratioPreset == ratio
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(if (isSelected) theme.terminalCyan.copy(alpha = 0.2f) else theme.surfaceVariant)
                .border(
                  0.5.dp,
                  if (isSelected) theme.terminalCyan else theme.surfaceBorder,
                  RoundedCornerShape(3.dp)
                )
                .clickable { onSelectRatio(ratio) }
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .testTag("ratio_preset_${ratio.name.lowercase()}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ratio.label,
                color = if (isSelected) theme.terminalCyan else theme.textSecondary,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }

          // Divider dot
          Box(
            modifier = Modifier
              .size(3.dp)
              .clip(CircleShape)
              .background(theme.surfaceBorder)
          )

          // Swap Positions Action
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(theme.surfaceVariant)
              .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(3.dp))
              .clickable { onSwapZones() }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("swap_zones_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Swap Zones",
              tint = theme.terminalMagenta,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = "SWAP",
              color = theme.terminalMagenta,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          // Spawn Triad Action
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(theme.accentPrimary.copy(alpha = 0.15f))
              .border(0.5.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
              .clickable { onSpawnDevTriad() }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("spawn_triad_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = "+ DEV TRIAD",
              color = theme.accentPrimary,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ZoneToggleCapsule(
  zone: TilingZoneType,
  isVisible: Boolean,
  theme: OmarchyThemeConfig,
  onClick: () -> Unit
) {
  val accent = when (zone) {
    TilingZoneType.TERMINAL -> theme.terminalGreen
    TilingZoneType.EDITOR -> theme.terminalCyan
    TilingZoneType.ASSISTANT -> theme.terminalMagenta
  }

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(3.dp))
      .background(if (isVisible) accent.copy(alpha = 0.18f) else theme.surfaceVariant.copy(alpha = 0.5f))
      .border(
        0.5.dp,
        if (isVisible) accent else theme.surfaceBorder,
        RoundedCornerShape(3.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 5.dp, vertical = 2.dp)
      .testTag("zone_toggle_${zone.name.lowercase()}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Icon(
      imageVector = zone.icon,
      contentDescription = null,
      tint = if (isVisible) accent else theme.textMuted,
      modifier = Modifier.size(10.dp)
    )
    Text(
      text = zone.shortLabel,
      color = if (isVisible) theme.textPrimary else theme.textMuted,
      fontSize = 8.5.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal
    )
    Icon(
      imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
      contentDescription = null,
      tint = if (isVisible) accent else theme.textMuted,
      modifier = Modifier.size(9.dp)
    )
  }
}

/**
 * TriZoneTilingLayout:
 * Splits the screen into distinct zones for Terminal, Editor, and Assistant components
 * according to the active TilingSplitPreset and SplitRatioPreset.
 */
@Composable
fun TriZoneTilingLayout(
  state: TilingControllerState,
  theme: OmarchyThemeConfig,
  terminalLines: List<TerminalLine>,
  neovimBuffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  neovimMode: String,
  agentMessages: List<AgentMessage>,
  isAgentThinking: Boolean,
  onFocusZone: (TilingZoneType) -> Unit,
  onToggleMaximizeZone: (TilingZoneType) -> Unit,
  onCloseZone: (TilingZoneType) -> Unit,
  onExecuteTerminalCommand: (String) -> Unit,
  onSelectNeovimBuffer: (Int) -> Unit,
  onUpdateNeovimContent: (String) -> Unit,
  onSetNeovimMode: (String) -> Unit,
  onOpenFileInNeovim: (String) -> Unit = {},
  onCloseNeovimBuffer: (Int) -> Unit = {},
  onExecuteNeovimCommand: (String) -> Unit = {},
  onSendAgentPrompt: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val renderZone: @Composable (TilingZoneType) -> Unit = { z ->
    RenderZoneContent(
      zoneType = z,
      theme = theme,
      terminalLines = terminalLines,
      neovimBuffers = neovimBuffers,
      activeBufferIndex = activeBufferIndex,
      neovimMode = neovimMode,
      agentMessages = agentMessages,
      isAgentThinking = isAgentThinking,
      onExecuteTerminalCommand = onExecuteTerminalCommand,
      onSelectNeovimBuffer = onSelectNeovimBuffer,
      onUpdateNeovimContent = onUpdateNeovimContent,
      onSetNeovimMode = onSetNeovimMode,
      onSendAgentPrompt = onSendAgentPrompt,
      onOpenFileInNeovim = onOpenFileInNeovim,
      onCloseNeovimBuffer = onCloseNeovimBuffer,
      onExecuteNeovimCommand = onExecuteNeovimCommand
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
      .testTag("tri_zone_tiling_layout")
  ) {
    // Check if any zone is maximized
    val maxZone = state.maximizedZone
    if (maxZone != null && state.isZoneVisible(maxZone)) {
      // Fullscreen maximized zone
      TilingZoneFrame(
        zoneType = maxZone,
        theme = theme,
        isFocused = true,
        isMaximized = true,
        onFocus = { onFocusZone(maxZone) },
        onToggleMaximize = { onToggleMaximizeZone(maxZone) },
        onClose = { onCloseZone(maxZone) },
        modifier = Modifier.fillMaxSize()
      ) {
        renderZone(maxZone)
      }
    } else {
      // Render layout based on preset
      val visibleList = state.zoneOrder.filter { state.isZoneVisible(it) }

      if (visibleList.isEmpty()) {
        // Empty state
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "All zones hidden. Use the layout controller to enable zones.",
            color = theme.textMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      } else if (visibleList.size == 1) {
        val single = visibleList.first()
        TilingZoneFrame(
          zoneType = single,
          theme = theme,
          isFocused = state.focusedZone == single,
          isMaximized = false,
          onFocus = { onFocusZone(single) },
          onToggleMaximize = { onToggleMaximizeZone(single) },
          onClose = { onCloseZone(single) },
          modifier = Modifier.fillMaxSize()
        ) {
          renderZone(single)
        }
      } else {
        // Multi-zone layout
        when (state.preset) {
          TilingSplitPreset.COCKPIT -> {
            CockpitLayout(
              zones = visibleList,
              focusedZone = state.focusedZone,
              theme = theme,
              weights = state.ratioPreset.weights,
              onFocusZone = onFocusZone,
              onToggleMaximizeZone = onToggleMaximizeZone,
              onCloseZone = onCloseZone,
              renderContent = { z -> renderZone(z) }
            )
          }
          TilingSplitPreset.TRI_COLUMNS -> {
            TriColumnsLayout(
              zones = visibleList,
              focusedZone = state.focusedZone,
              theme = theme,
              weights = state.ratioPreset.weights,
              onFocusZone = onFocusZone,
              onToggleMaximizeZone = onToggleMaximizeZone,
              onCloseZone = onCloseZone,
              renderContent = { z -> renderZone(z) }
            )
          }
          TilingSplitPreset.SIDEBAR_RIGHT -> {
            SidebarLayout(
              isRightSidebar = true,
              zones = visibleList,
              focusedZone = state.focusedZone,
              theme = theme,
              onFocusZone = onFocusZone,
              onToggleMaximizeZone = onToggleMaximizeZone,
              onCloseZone = onCloseZone,
              renderContent = { z -> renderZone(z) }
            )
          }
          TilingSplitPreset.SIDEBAR_LEFT -> {
            SidebarLayout(
              isRightSidebar = false,
              zones = visibleList,
              focusedZone = state.focusedZone,
              theme = theme,
              onFocusZone = onFocusZone,
              onToggleMaximizeZone = onToggleMaximizeZone,
              onCloseZone = onCloseZone,
              renderContent = { z -> renderZone(z) }
            )
          }
          TilingSplitPreset.GRID_2X2 -> {
            GridSplitLayout(
              zones = visibleList,
              focusedZone = state.focusedZone,
              theme = theme,
              onFocusZone = onFocusZone,
              onToggleMaximizeZone = onToggleMaximizeZone,
              onCloseZone = onCloseZone,
              renderContent = { z -> renderZone(z) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CockpitLayout(
  zones: List<TilingZoneType>,
  focusedZone: TilingZoneType,
  theme: OmarchyThemeConfig,
  weights: Triple<Float, Float, Float>,
  onFocusZone: (TilingZoneType) -> Unit,
  onToggleMaximizeZone: (TilingZoneType) -> Unit,
  onCloseZone: (TilingZoneType) -> Unit,
  renderContent: @Composable (TilingZoneType) -> Unit
) {
  val topZone = zones.firstOrNull { it == TilingZoneType.EDITOR } ?: zones.first()
  val bottomZones = zones.filter { it != topZone }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    // Top Primary Zone
    Box(
      modifier = Modifier
        .weight(if (bottomZones.isEmpty()) 1f else weights.second * 2.2f)
        .fillMaxWidth()
    ) {
      TilingZoneFrame(
        zoneType = topZone,
        theme = theme,
        isFocused = focusedZone == topZone,
        isMaximized = false,
        onFocus = { onFocusZone(topZone) },
        onToggleMaximize = { onToggleMaximizeZone(topZone) },
        onClose = { onCloseZone(topZone) },
        modifier = Modifier.fillMaxSize()
      ) {
        renderContent(topZone)
      }
    }

    // Bottom Split Zones
    if (bottomZones.isNotEmpty()) {
      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        bottomZones.forEach { bZone ->
          val w = if (bZone == TilingZoneType.TERMINAL) weights.first else weights.third
          Box(
            modifier = Modifier
              .weight(w.coerceAtLeast(0.5f))
              .fillMaxHeight()
          ) {
            TilingZoneFrame(
              zoneType = bZone,
              theme = theme,
              isFocused = focusedZone == bZone,
              isMaximized = false,
              onFocus = { onFocusZone(bZone) },
              onToggleMaximize = { onToggleMaximizeZone(bZone) },
              onClose = { onCloseZone(bZone) },
              modifier = Modifier.fillMaxSize()
            ) {
              renderContent(bZone)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TriColumnsLayout(
  zones: List<TilingZoneType>,
  focusedZone: TilingZoneType,
  theme: OmarchyThemeConfig,
  weights: Triple<Float, Float, Float>,
  onFocusZone: (TilingZoneType) -> Unit,
  onToggleMaximizeZone: (TilingZoneType) -> Unit,
  onCloseZone: (TilingZoneType) -> Unit,
  renderContent: @Composable (TilingZoneType) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    zones.forEach { z ->
      val w = when (z) {
        TilingZoneType.TERMINAL -> weights.first
        TilingZoneType.EDITOR -> weights.second
        TilingZoneType.ASSISTANT -> weights.third
      }

      Box(
        modifier = Modifier
          .weight(w.coerceAtLeast(0.3f))
          .fillMaxHeight()
      ) {
        TilingZoneFrame(
          zoneType = z,
          theme = theme,
          isFocused = focusedZone == z,
          isMaximized = false,
          onFocus = { onFocusZone(z) },
          onToggleMaximize = { onToggleMaximizeZone(z) },
          onClose = { onCloseZone(z) },
          modifier = Modifier.fillMaxSize()
        ) {
          renderContent(z)
        }
      }
    }
  }
}

@Composable
private fun SidebarLayout(
  isRightSidebar: Boolean,
  zones: List<TilingZoneType>,
  focusedZone: TilingZoneType,
  theme: OmarchyThemeConfig,
  onFocusZone: (TilingZoneType) -> Unit,
  onToggleMaximizeZone: (TilingZoneType) -> Unit,
  onCloseZone: (TilingZoneType) -> Unit,
  renderContent: @Composable (TilingZoneType) -> Unit
) {
  val sidebarZone = if (isRightSidebar) {
    zones.firstOrNull { it == TilingZoneType.ASSISTANT } ?: zones.last()
  } else {
    zones.firstOrNull { it == TilingZoneType.TERMINAL } ?: zones.first()
  }
  val mainZones = zones.filter { it != sidebarZone }

  Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    if (!isRightSidebar) {
      // Left Sidebar
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        TilingZoneFrame(
          zoneType = sidebarZone,
          theme = theme,
          isFocused = focusedZone == sidebarZone,
          isMaximized = false,
          onFocus = { onFocusZone(sidebarZone) },
          onToggleMaximize = { onToggleMaximizeZone(sidebarZone) },
          onClose = { onCloseZone(sidebarZone) },
          modifier = Modifier.fillMaxSize()
        ) {
          renderContent(sidebarZone)
        }
      }
    }

    // Main Column
    Column(
      modifier = Modifier.weight(1.8f).fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      mainZones.forEach { mZone ->
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          TilingZoneFrame(
            zoneType = mZone,
            theme = theme,
            isFocused = focusedZone == mZone,
            isMaximized = false,
            onFocus = { onFocusZone(mZone) },
            onToggleMaximize = { onToggleMaximizeZone(mZone) },
            onClose = { onCloseZone(mZone) },
            modifier = Modifier.fillMaxSize()
          ) {
            renderContent(mZone)
          }
        }
      }
    }

    if (isRightSidebar) {
      // Right Sidebar
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        TilingZoneFrame(
          zoneType = sidebarZone,
          theme = theme,
          isFocused = focusedZone == sidebarZone,
          isMaximized = false,
          onFocus = { onFocusZone(sidebarZone) },
          onToggleMaximize = { onToggleMaximizeZone(sidebarZone) },
          onClose = { onCloseZone(sidebarZone) },
          modifier = Modifier.fillMaxSize()
        ) {
          renderContent(sidebarZone)
        }
      }
    }
  }
}

@Composable
private fun GridSplitLayout(
  zones: List<TilingZoneType>,
  focusedZone: TilingZoneType,
  theme: OmarchyThemeConfig,
  onFocusZone: (TilingZoneType) -> Unit,
  onToggleMaximizeZone: (TilingZoneType) -> Unit,
  onCloseZone: (TilingZoneType) -> Unit,
  renderContent: @Composable (TilingZoneType) -> Unit
) {
  val topZones = zones.take(if (zones.size > 2) 1 else 1)
  val bottomZones = zones.drop(topZones.size)

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    // Top Row
    Row(
      modifier = Modifier.weight(1.2f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      topZones.forEach { z ->
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          TilingZoneFrame(
            zoneType = z,
            theme = theme,
            isFocused = focusedZone == z,
            isMaximized = false,
            onFocus = { onFocusZone(z) },
            onToggleMaximize = { onToggleMaximizeZone(z) },
            onClose = { onCloseZone(z) },
            modifier = Modifier.fillMaxSize()
          ) {
            renderContent(z)
          }
        }
      }
    }

    // Bottom Row
    if (bottomZones.isNotEmpty()) {
      Row(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        bottomZones.forEach { z ->
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TilingZoneFrame(
              zoneType = z,
              theme = theme,
              isFocused = focusedZone == z,
              isMaximized = false,
              onFocus = { onFocusZone(z) },
              onToggleMaximize = { onToggleMaximizeZone(z) },
              onClose = { onCloseZone(z) },
              modifier = Modifier.fillMaxSize()
            ) {
              renderContent(z)
            }
          }
        }
      }
    }
  }
}

/**
 * Individual window frame representing a distinct functional zone.
 */
@Composable
private fun TilingZoneFrame(
  zoneType: TilingZoneType,
  theme: OmarchyThemeConfig,
  isFocused: Boolean,
  isMaximized: Boolean,
  onFocus: () -> Unit,
  onToggleMaximize: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val zoneColor = when (zoneType) {
    TilingZoneType.TERMINAL -> theme.terminalGreen
    TilingZoneType.EDITOR -> theme.terminalCyan
    TilingZoneType.ASSISTANT -> theme.terminalMagenta
  }

  val borderWidth by animateDpAsState(if (isFocused) 1.5.dp else 1.dp, label = "zone_bw")
  val borderColor by animateColorAsState(
    if (isFocused) zoneColor else theme.surfaceBorder,
    label = "zone_bc"
  )

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(theme.terminalBackground)
      .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
      .clickable { onFocus() }
      .testTag("tiling_zone_${zoneType.name.lowercase()}")
  ) {
    // Zone Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(26.dp)
        .background(if (isFocused) theme.surfaceVariant else theme.surfaceColor)
        .border(0.5.dp, theme.surfaceBorder)
        .padding(horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Zone Icon & Label
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        Icon(
          imageVector = zoneType.icon,
          contentDescription = null,
          tint = zoneColor,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = zoneType.title.uppercase(),
          color = if (isFocused) theme.textPrimary else theme.textSecondary,
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Zone Controls: Maximize & Close/Hide
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Maximize/Restore
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .clickable { onToggleMaximize() }
            .testTag("zone_maximize_${zoneType.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = "Toggle Maximize",
            tint = theme.textSecondary,
            modifier = Modifier.size(12.dp)
          )
        }

        // Hide/Close
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .clickable { onClose() }
            .testTag("zone_close_${zoneType.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Hide Zone",
            tint = theme.terminalRed.copy(alpha = 0.8f),
            modifier = Modifier.size(11.dp)
          )
        }
      }
    }

    // Zone Content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      content()
    }
  }
}

/**
 * Dispatches and renders the corresponding Omarchy application inside the zone.
 */
@Composable
private fun RenderZoneContent(
  zoneType: TilingZoneType,
  theme: OmarchyThemeConfig,
  terminalLines: List<TerminalLine>,
  neovimBuffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  neovimMode: String,
  agentMessages: List<AgentMessage>,
  isAgentThinking: Boolean,
  onExecuteTerminalCommand: (String) -> Unit,
  onSelectNeovimBuffer: (Int) -> Unit,
  onUpdateNeovimContent: (String) -> Unit,
  onSetNeovimMode: (String) -> Unit,
  onSendAgentPrompt: (String) -> Unit,
  onOpenFileInNeovim: (String) -> Unit = {},
  onCloseNeovimBuffer: (Int) -> Unit = {},
  onExecuteNeovimCommand: (String) -> Unit = {}
) {
  when (zoneType) {
    TilingZoneType.TERMINAL -> {
      TerminalApp(
        theme = theme,
        lines = terminalLines,
        onExecuteCommand = onExecuteTerminalCommand,
        modifier = Modifier.fillMaxSize()
      )
    }
    TilingZoneType.EDITOR -> {
      NeovimApp(
        theme = theme,
        buffers = neovimBuffers,
        activeBufferIndex = activeBufferIndex,
        neovimMode = neovimMode,
        onSelectBuffer = onSelectNeovimBuffer,
        onContentChange = onUpdateNeovimContent,
        onModeChange = onSetNeovimMode,
        onOpenFile = onOpenFileInNeovim,
        onCloseBuffer = onCloseNeovimBuffer,
        onExecuteCommand = onExecuteNeovimCommand,
        modifier = Modifier.fillMaxSize()
      )
    }
    TilingZoneType.ASSISTANT -> {
      AgenticConsoleApp(
        theme = theme,
        messages = agentMessages,
        isThinking = isAgentThinking,
        onSendMessage = onSendAgentPrompt,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
