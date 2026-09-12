package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.ui.KeyboardShortcutsHud
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KeyboardShortcutsHudTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val testTheme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

  @Test
  fun testHudDisplaysAndCanBeInteractedWith() {
    var collapsedToggled = false
    var terminalLaunched = false

    val testWindow = WindowInstance(
      id = "win-term",
      appType = AppType.TERMINAL,
      title = "Terminal",
      workspaceId = 1,
      isFocused = true
    )

    composeTestRule.setContent {
      KeyboardShortcutsHud(
        theme = testTheme,
        focusedWindow = testWindow,
        isWalkerOpen = false,
        isControlCenterOpen = false,
        isKeybindsOpen = false,
        isCollapsed = false,
        layoutMode = WindowLayoutMode.SPLIT_VERTICAL,
        isFullscreen = false,
        neovimMode = "NORMAL",
        onToggleCollapse = { collapsedToggled = true },
        onClose = {},
        onLaunchTerminal = { terminalLaunched = true },
        onOpenWalker = {},
        onCloseWindow = {},
        onToggleFullscreen = {},
        onToggleLayout = {},
        onExecuteCmd = {},
        onSetNeovimMode = {},
        onToggleObsidianPreview = {},
        onCreateNote = {},
        onCycleTheme = {}
      )
    }

    composeTestRule.onNodeWithTag("hud_expanded_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hud_collapse_button").performClick()
    assertTrue(collapsedToggled)
  }
}
