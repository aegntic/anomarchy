package com.example.omarchy.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Distinct functional zones managed by the tiling window layout controller.
 */
enum class TilingZoneType(
  val title: String,
  val shortLabel: String,
  val defaultAppType: AppType
) {
  TERMINAL("Terminal Zone", "Terminal", AppType.TERMINAL),
  EDITOR("Editor Zone", "Editor", AppType.NEOVIM),
  ASSISTANT("Assistant Zone", "AI Assistant", AppType.AGENT);

  val icon: ImageVector
    get() = when (this) {
      TERMINAL -> Icons.Default.Terminal
      EDITOR -> Icons.Default.Code
      ASSISTANT -> Icons.Default.Psychology
    }
}

/**
 * Tiling layout arrangement presets for splitting screen space into zones.
 */
enum class TilingSplitPreset(
  val displayName: String,
  val shortCode: String,
  val description: String,
  val icon: ImageVector
) {
  COCKPIT(
    displayName = "Cockpit",
    shortCode = "Cockpit",
    description = "Editor on top, Terminal and Assistant side-by-side below",
    icon = Icons.Default.ViewQuilt
  ),
  TRI_COLUMNS(
    displayName = "3-Columns",
    shortCode = "Columns",
    description = "Terminal | Editor | Assistant side-by-side vertical columns",
    icon = Icons.Default.ViewColumn
  ),
  SIDEBAR_RIGHT(
    displayName = "Right Sidebar",
    shortCode = "AI Sidebar",
    description = "Editor & Terminal stacked on left, Assistant sidebar on right",
    icon = Icons.Default.ViewSidebar
  ),
  SIDEBAR_LEFT(
    displayName = "Left Sidebar",
    shortCode = "Term Sidebar",
    description = "Terminal sidebar on left, Editor & Assistant stacked on right",
    icon = Icons.Default.ViewAgenda
  ),
  GRID_2X2(
    displayName = "Quad Grid",
    shortCode = "Grid",
    description = "Balanced grid layout with prioritized editor space",
    icon = Icons.Default.GridView
  )
}

/**
 * Proportional split weights for screen real-estate allocation.
 */
enum class SplitRatioPreset(
  val label: String,
  val weights: Triple<Float, Float, Float> // Terminal, Editor, Assistant
) {
  BALANCED("1:1:1", Triple(1f, 1f, 1f)),
  EDITOR_FOCUS("50/25/25", Triple(0.25f, 0.50f, 0.25f)),
  DEV_PAIRING("40/40/20", Triple(0.40f, 0.40f, 0.20f)),
  TERMINAL_FOCUS("50/30/20", Triple(0.50f, 0.30f, 0.20f))
}

/**
 * State of an individual zone within the layout controller.
 */
data class TilingZoneConfig(
  val zoneType: TilingZoneType,
  val isVisible: Boolean = true,
  val isMaximized: Boolean = false,
  val customTitle: String? = null
)

/**
 * Global state for the tiling window management layout controller.
 */
data class TilingControllerState(
  val preset: TilingSplitPreset = TilingSplitPreset.COCKPIT,
  val ratioPreset: SplitRatioPreset = SplitRatioPreset.EDITOR_FOCUS,
  val zoneOrder: List<TilingZoneType> = listOf(
    TilingZoneType.EDITOR,
    TilingZoneType.TERMINAL,
    TilingZoneType.ASSISTANT
  ),
  val visibleZones: Set<TilingZoneType> = setOf(
    TilingZoneType.EDITOR,
    TilingZoneType.TERMINAL,
    TilingZoneType.ASSISTANT
  ),
  val maximizedZone: TilingZoneType? = null,
  val focusedZone: TilingZoneType = TilingZoneType.EDITOR,
  val isControllerExpanded: Boolean = true
) {
  val activeZoneCount: Int
    get() = visibleZones.size

  fun isZoneVisible(zone: TilingZoneType): Boolean = visibleZones.contains(zone)

  fun isZoneMaximized(zone: TilingZoneType): Boolean = maximizedZone == zone
}
