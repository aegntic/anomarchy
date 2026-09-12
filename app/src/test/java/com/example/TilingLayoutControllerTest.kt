package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.SplitRatioPreset
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.TilingControllerState
import com.example.omarchy.model.TilingSplitPreset
import com.example.omarchy.model.TilingZoneType
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.state.OmarchyViewModel
import com.example.omarchy.ui.TilingLayoutControllerBar
import com.example.omarchy.ui.TriZoneTilingLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TilingLayoutControllerTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val testTheme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

  @Test
  fun testTilingControllerBarRendersAndHandlesInteractions() {
    var selectedPreset: TilingSplitPreset? = null
    var selectedRatio: SplitRatioPreset? = null
    var toggledZone: TilingZoneType? = null
    var swapped = false
    var spawnedTriad = false
    var collapsedToggled = false

    val state = TilingControllerState(
      preset = TilingSplitPreset.COCKPIT,
      ratioPreset = SplitRatioPreset.EDITOR_FOCUS,
      isControllerExpanded = true
    )

    composeTestRule.setContent {
      TilingLayoutControllerBar(
        state = state,
        theme = testTheme,
        onSelectPreset = { selectedPreset = it },
        onSelectRatio = { selectedRatio = it },
        onToggleZone = { toggledZone = it },
        onSwapZones = { swapped = true },
        onSpawnDevTriad = { spawnedTriad = true },
        onToggleExpanded = { collapsedToggled = true }
      )
    }

    // Verify presence of controller bar and elements
    composeTestRule.onNodeWithTag("tiling_layout_controller_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tiling_controller_expanded_panel").assertIsDisplayed()

    // Test clicking 3-Columns preset
    composeTestRule.onNodeWithTag("preset_button_tri_columns").performScrollTo().performClick()
    assertEquals(TilingSplitPreset.TRI_COLUMNS, selectedPreset)

    // Test clicking a ratio preset
    composeTestRule.onNodeWithTag("ratio_preset_balanced").performScrollTo().performClick()
    assertEquals(SplitRatioPreset.BALANCED, selectedRatio)

    // Test clicking a zone toggle
    composeTestRule.onNodeWithTag("zone_toggle_terminal").performScrollTo().performClick()
    assertEquals(TilingZoneType.TERMINAL, toggledZone)

    // Test swap action
    composeTestRule.onNodeWithTag("swap_zones_button").performScrollTo().performClick()
    assertTrue(swapped)

    // Test spawn triad action
    composeTestRule.onNodeWithTag("spawn_triad_button").performScrollTo().performClick()
    assertTrue(spawnedTriad)
  }

  @Test
  fun testTriZoneTilingLayoutRendersAllZones() {
    val state = TilingControllerState(
      preset = TilingSplitPreset.COCKPIT,
      visibleZones = setOf(TilingZoneType.EDITOR, TilingZoneType.TERMINAL, TilingZoneType.ASSISTANT)
    )

    composeTestRule.setContent {
      TriZoneTilingLayout(
        state = state,
        theme = testTheme,
        terminalLines = emptyList(),
        neovimBuffers = emptyList(),
        activeBufferIndex = 0,
        neovimMode = "NORMAL",
        agentMessages = emptyList(),
        isAgentThinking = false,
        onFocusZone = {},
        onToggleMaximizeZone = {},
        onCloseZone = {},
        onExecuteTerminalCommand = {},
        onSelectNeovimBuffer = {},
        onUpdateNeovimContent = {},
        onSetNeovimMode = {},
        onSendAgentPrompt = {}
      )
    }

    composeTestRule.onNodeWithTag("tri_zone_tiling_layout").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tiling_zone_editor").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tiling_zone_terminal").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tiling_zone_assistant").assertIsDisplayed()
  }

  @Test
  fun testViewModelTilingManagement() {
    val viewModel = OmarchyViewModel()

    // Test changing preset
    viewModel.setTilingPreset(TilingSplitPreset.TRI_COLUMNS)
    assertEquals(TilingSplitPreset.TRI_COLUMNS, viewModel.tilingControllerState.value.preset)
    assertEquals(WindowLayoutMode.TRI_ZONE_DEV, viewModel.layoutMode.value)

    // Test changing ratio
    viewModel.setTilingSplitRatio(SplitRatioPreset.BALANCED)
    assertEquals(SplitRatioPreset.BALANCED, viewModel.tilingControllerState.value.ratioPreset)

    // Test zone visibility toggling
    assertTrue(viewModel.tilingControllerState.value.isZoneVisible(TilingZoneType.TERMINAL))
    viewModel.toggleTilingZoneVisibility(TilingZoneType.TERMINAL)
    assertFalse(viewModel.tilingControllerState.value.isZoneVisible(TilingZoneType.TERMINAL))
    viewModel.toggleTilingZoneVisibility(TilingZoneType.TERMINAL)
    assertTrue(viewModel.tilingControllerState.value.isZoneVisible(TilingZoneType.TERMINAL))

    // Test maximize toggle
    assertNull(viewModel.tilingControllerState.value.maximizedZone)
    viewModel.toggleTilingZoneMaximized(TilingZoneType.EDITOR)
    assertEquals(TilingZoneType.EDITOR, viewModel.tilingControllerState.value.maximizedZone)
    viewModel.toggleTilingZoneMaximized(TilingZoneType.EDITOR)
    assertNull(viewModel.tilingControllerState.value.maximizedZone)

    // Test swapping zone positions
    val initialFirst = viewModel.tilingControllerState.value.zoneOrder[0]
    val initialSecond = viewModel.tilingControllerState.value.zoneOrder[1]
    viewModel.swapTilingZones()
    assertEquals(initialSecond, viewModel.tilingControllerState.value.zoneOrder[0])
    assertEquals(initialFirst, viewModel.tilingControllerState.value.zoneOrder[1])

    // Test spawnDevTriad launches apps and activates TRI_ZONE_DEV
    viewModel.spawnDevTriad()
    assertEquals(WindowLayoutMode.TRI_ZONE_DEV, viewModel.layoutMode.value)
    val wins = viewModel.windows.value.filter { it.workspaceId == viewModel.currentWorkspace.value }
    assertTrue(wins.any { it.appType == AppType.TERMINAL })
    assertTrue(wins.any { it.appType == AppType.NEOVIM })
    assertTrue(wins.any { it.appType == AppType.AGENT })
  }
}
