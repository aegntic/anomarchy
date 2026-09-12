package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.ui.QuickshellBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w800dp-h1280dp")
class QuickshellBarTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val testTheme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

  @Test
  fun testQuickshellBarDisplaysIslandsAndHandlesInteractions() {
    var selectedWorkspace: Int? = null
    var controlCenterToggled = false
    var walkerOpened = false
    var layoutToggled = false
    var btopLaunched = false

    val windows = listOf(
      WindowInstance(id = "win-1", appType = AppType.TERMINAL, title = "alacritty: zsh", workspaceId = 1, isFocused = true),
      WindowInstance(id = "win-2", appType = AppType.NEOVIM, title = "main.rs - Neovim", workspaceId = 2)
    )

    composeTestRule.setContent {
      QuickshellBar(
        theme = testTheme,
        currentWorkspace = 1,
        windows = windows,
        focusedWindow = windows[0],
        layoutMode = WindowLayoutMode.TRI_ZONE_DEV,
        cpuPercent = 28f,
        memPercent = 45f,
        isHudOpen = true,
        cpuHistory = listOf(15f, 20f, 28f),
        onWorkspaceSelect = { selectedWorkspace = it },
        onToggleControlCenter = { controlCenterToggled = true },
        onToggleKeybinds = {},
        onToggleHud = {},
        onOpenWalker = { walkerOpened = true },
        onToggleLayoutMode = { layoutToggled = true },
        onLaunchBtop = { btopLaunched = true }
      )
    }

    // Verify main status bar and islands are displayed
    composeTestRule.onNodeWithTag("quickshell_status_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quickshell_workspaces_island").assertIsDisplayed()
    composeTestRule.onNodeWithTag("active_window_island").assertIsDisplayed()
    composeTestRule.onNodeWithTag("system_metrics_pill").assertIsDisplayed()

    // Verify CPU and RAM labels
    composeTestRule.onNodeWithText("CPU 28%").assertIsDisplayed()
    composeTestRule.onNodeWithText("RAM 45%").assertIsDisplayed()

    // Test clicking workspace 3
    composeTestRule.onNodeWithTag("workspace_tab_3").performClick()
    assertEquals(3, selectedWorkspace)

    // Test layout mode button click
    composeTestRule.onNodeWithTag("layout_mode_button").performClick()
    assertTrue(layoutToggled)

    // Test walker button click
    composeTestRule.onNodeWithTag("walker_launcher_icon").performClick()
    assertTrue(walkerOpened)

    // Test logo button click
    composeTestRule.onNodeWithTag("omarchy_logo_button").performClick()
    assertTrue(controlCenterToggled)

    // Test clicking metrics pill to open telemetry flyout
    composeTestRule.onNodeWithTag("system_metrics_pill").performClick()
    composeTestRule.onNodeWithTag("quickshell_telemetry_flyout").assertIsDisplayed()
    composeTestRule.onNodeWithTag("launch_btop_from_quickshell").performClick()
    assertTrue(btopLaunched)
  }
}
